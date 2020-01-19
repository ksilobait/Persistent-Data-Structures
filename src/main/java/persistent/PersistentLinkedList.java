package persistent;

import java.util.ArrayList;
import java.util.SortedSet;
import java.util.TreeSet;

public class PersistentLinkedList<T> {

    final Node root;
    final int branchingFactor;
    final int depth;
    final int base; //BF ^ (depth - 1)
    final int size;
    SortedSet<Integer> unusedIndices = new TreeSet<>();
    int indexCorrespondingToTheFirstElement;
    int indexCorrespondingToTheLatestElement;

    /**
     * fat node in the graph
     */
    class Node {

        ArrayList<Node> children;
        T data;
        int previousIndex;
        int nextIndex;

        /**
         * constructor for internal (non-leaf) nodes
         */
        Node() {
            this.data = null;
            this.children = new ArrayList<>();
            for (int i = 0; i < branchingFactor; i++) {
                this.children.add(null);
            }
            this.previousIndex = -1;
            this.nextIndex = -1;
        }

        /**
         * constructor for leaf nodes
         *
         * @param data data to be stored in the leaf
         */
        Node(T data) {
            this.data = data;
            this.children = null;
            this.previousIndex = -1;
            this.nextIndex = -1;
        }

        /**
         * get the ith child in the current node
         *
         * @param i index of the needed child
         * @return the ith child
         */
        Node get(int i) {
            if (this.children.size() <= i) {
                return null;
            }
            return this.children.get(i);
        }

        /**
         * set the ith child
         *
         * @param i index of the needed child
         * @param e new value of the child
         */
        void set(int i, Node e) {
            this.children.set(i, e);
        }

    }

    /**
     * package-private constructor for the persistent array
     *
     * @param root a designated/initial vertex in a graph
     * @param branchingFactor number of children at each node
     * @param depth maximum number of edges in the paths from the root to any node
     * @param base branchingFactor ^ (depth - 1)
     * @param size number of leaves in the graph or elements in the persistent array
     */
    PersistentLinkedList(Node root, int branchingFactor, int depth, int base, int size,
        SortedSet<Integer> unusedIndices, int indexCorrespondingToTheFirstElement,
        int indexCorrespondingToTheLatestElement) {
        this.root = root;
        this.branchingFactor = branchingFactor;
        this.depth = depth;
        this.base = base;
        this.size = size;
        this.unusedIndices.addAll(unusedIndices);
        this.indexCorrespondingToTheFirstElement = indexCorrespondingToTheFirstElement;
        this.indexCorrespondingToTheLatestElement = indexCorrespondingToTheLatestElement;
    }

    /**
     * constructor for the persistent array
     *
     * @param data initial value for the array
     * @param powerOfBranchingFactor the branching factor will be equals to
     * 2^powerOfBranchingFactor
     */
    public PersistentLinkedList(T data, int powerOfBranchingFactor) {
        int branchingFactor = 1;
        for (int i = 0; i < powerOfBranchingFactor; i++) {
            branchingFactor *= 2;
        }

        this.branchingFactor = branchingFactor;
        this.root = new Node();
        this.root.set(0, new Node(data));
        this.depth = 1;
        this.base = 1;
        this.size = 1;
        this.indexCorrespondingToTheFirstElement = 0;
        this.indexCorrespondingToTheLatestElement = 0;
    }

    private void setNewIndexCorrespondingToTheFirstElement(int dataIndex) {
        this.indexCorrespondingToTheFirstElement = dataIndex;
    }

    private void setNewIndexCorrespondingToTheLatestElement(int dataIndex) {
        this.indexCorrespondingToTheLatestElement = dataIndex;
    }

    private void changeUnusedIndices(int index, boolean notUnusedAnymore) {
        if (notUnusedAnymore) {
            this.unusedIndices.remove(index);
        } else {
            if (index < this.size) {
                this.unusedIndices.add(index);
            }
        }
    }

    private class TraverseData {

        Node currentNode;
        Node currentNewNode;
        Node newRoot;
        int index;

        int base;

        public TraverseData(Node currentNode, Node currentNewNode, Node newRoot, int index,
            int base) {
            this.currentNode = currentNode;
            this.currentNewNode = currentNewNode;
            this.newRoot = newRoot;
            this.index = index;
            this.base = base;
        }

    }

    /**
     * traverse one level in the graph with copying
     *
     * @param data metadata before this level
     * @return metadata after this level
     */
    private TraverseData traverseOneLevel(TraverseData data) {
        Node currentNode = data.currentNode;
        Node currentNewNode = data.currentNewNode;
        int nextBranch = data.index / data.base;

        currentNewNode.set(nextBranch, new Node());
        for (int anotherBranch = 0; anotherBranch < branchingFactor; anotherBranch++) {
            if (anotherBranch == nextBranch) {
                continue;
            }
            currentNewNode.set(anotherBranch, currentNode.get(anotherBranch));
        }

        //down
        currentNode = currentNode.get(nextBranch);
        currentNewNode = currentNewNode.get(nextBranch);
        return new TraverseData(currentNode, currentNewNode, data.newRoot, data.index % data.base,
            data.base);
    }

    /**
     * traverse the old structure while creating the new one and copying data into it
     *
     * @param index destination index in the array
     * @return metadata of traversing
     */
    private TraverseData traverse(int index) {
        Node newRoot = new Node();
        Node currentNode = this.root;
        Node currentNewNode = newRoot;

        for (int b = base; b > 1; b = b / branchingFactor) {
            TraverseData data = traverseOneLevel(
                new TraverseData(currentNode, currentNewNode, newRoot, index, b));
            currentNode = data.currentNode;
            currentNewNode = data.currentNewNode;
            index = data.index;
        }
        return new TraverseData(currentNode, currentNewNode, newRoot, index, 1);
    }

    private int searchIndex(int listIndex) {
        int currentTreeIndex = this.indexCorrespondingToTheFirstElement;
        Node currentNode = getHelper(currentTreeIndex);
        for (int i = 0; i < listIndex; i++) {
            currentTreeIndex = currentNode.nextIndex;
            currentNode = getHelper(currentTreeIndex);
        }
        return currentTreeIndex;
    }

    private Node getHelper(int index) {
        Node currentNode = this.root;

        for (int b = base; b > 1; b = b / branchingFactor) {
            int nextBranch = index / b;

            //down
            currentNode = currentNode.get(nextBranch);
            index = index % b;
        }
        return currentNode.get(index);
    }

    public T getFirst() {
        return this.getHelper(this.indexCorrespondingToTheFirstElement).data;
    }

    public T getLast() {
        return this.getHelper(this.indexCorrespondingToTheLatestElement).data;
    }

    /*public T get(int index) {
        return this.getHelper(search(index));
    }*/

    public PersistentLinkedList<T> addHelper(T data) {
        //there's still space in the latest element
        if (this.size % branchingFactor != 0) {
            return setHelper(this.size, data);
        }

        //there's still space for the new data
        if (this.base * branchingFactor > this.size) {
            Node newRoot = new Node();

            Node currentNode = this.root;
            Node currentNewNode = newRoot;

            int index = this.size;
            int b;
            for (b = base; b > 0; b = b / branchingFactor) {
                TraverseData traverseData = traverseOneLevel(
                    new TraverseData(currentNode, currentNewNode, newRoot, index, b));
                currentNode = traverseData.currentNode;
                currentNewNode = traverseData.currentNewNode;
                index = traverseData.index;

                if (currentNode == null) {
                    b = b / branchingFactor;
                    break;
                }
            }

            while (b > 1) {
                currentNewNode.set(0, new Node());
                currentNewNode = currentNewNode.get(0);
                index = index % b;
                b = b / branchingFactor;
            }
            currentNewNode.set(0, new Node(data));

            return new PersistentLinkedList<>(newRoot, this.branchingFactor, this.depth, this.base,
                this.size + 1, unusedIndices, indexCorrespondingToTheFirstElement,
                indexCorrespondingToTheLatestElement);
        }

        //root overflow
        Node newRoot = new Node();
        newRoot.set(0, this.root);
        newRoot.set(1, new Node());
        //newRoot[2..]=null
        Node currentNewNode = newRoot.get(1);

        int b = base;
        while (b > 1) {
            currentNewNode.set(0, new Node());
            currentNewNode = currentNewNode.get(0);
            b = b / branchingFactor;
        }
        currentNewNode.set(0, new Node(data));

        return new PersistentLinkedList<>(newRoot, this.branchingFactor, this.depth + 1,
            this.base * branchingFactor, this.size + 1, unusedIndices,
            indexCorrespondingToTheFirstElement, indexCorrespondingToTheLatestElement);
    }

    private PersistentLinkedList<T> setHelper(int index, T data) {
        int newSize = this.size;
        if (newSize == index) {
            newSize++;
        }

        TraverseData traverseData = traverse(index);

        traverseData.currentNewNode.set(traverseData.index, new Node(data));
        for (int i = 0; i < branchingFactor; i++) {
            if (i == traverseData.index) {
                continue;
            }
            traverseData.currentNewNode.set(i, traverseData.currentNode.get(i));
        }

        return new PersistentLinkedList<>(traverseData.newRoot, branchingFactor, depth, base,
            newSize, unusedIndices, indexCorrespondingToTheFirstElement,
            indexCorrespondingToTheLatestElement);
    }

    public PersistentLinkedList<T> add(int index, T data) {
        int beforeTreeIndex;
        int afterTreeIndex;
        if (index == 0) {
            beforeTreeIndex = -1;
            afterTreeIndex = this.indexCorrespondingToTheFirstElement;
        } else if (index == this.size) {
            beforeTreeIndex = this.indexCorrespondingToTheLatestElement;
            afterTreeIndex = -1;
        } else {
            afterTreeIndex = searchIndex(index);
            beforeTreeIndex = getHelper(afterTreeIndex).previousIndex;
        }

        int newElementTreeIndex;
        PersistentLinkedList<T> newVersion;
        if (unusedIndices.isEmpty()) {
            newElementTreeIndex = this.size;
            newVersion = this.addHelper(data);
        } else {
            newElementTreeIndex = unusedIndices.first();
            newVersion = this.setHelper(newElementTreeIndex, data);
        }
        newVersion.changeUnusedIndices(newElementTreeIndex, true);

        if (beforeTreeIndex != -1) {
            newVersion = newVersion.changeLinks(beforeTreeIndex, newElementTreeIndex);
        } else {
            newVersion.setNewIndexCorrespondingToTheFirstElement(newElementTreeIndex);
        }

        if (afterTreeIndex != -1) {
            newVersion = newVersion.changeLinks(newElementTreeIndex, afterTreeIndex);
        } else {
            newVersion.setNewIndexCorrespondingToTheLatestElement(newElementTreeIndex);
        }

        return newVersion;
    }

    public PersistentLinkedList<T> addFirst(T data) {
        return add(0, data);
    }

    public PersistentLinkedList<T> addLast(T data) {
        return add(this.size, data);
    }

    private PersistentLinkedList<T> changeLinks(int from, int to) {
        PersistentLinkedList<T> newVersion = this.setLinksHelper(from, to, false);
        return newVersion.setLinksHelper(to, from, true);
        /*Node fromNode = getHelper(from);
        Node toNode = getHelper(to);
        fromNode.nextIndex = to;
        toNode.previousIndex = from;*/
    }

    PersistentLinkedList<T> setLinksHelper(int index, int data, boolean setPreviousIndex) {
        TraverseData traverseData = traverse(index);
        int finalIndex = traverseData.index;
        traverseData.currentNewNode
            .set(finalIndex, new Node(traverseData.currentNode.get(finalIndex).data));
        if (setPreviousIndex) {
            traverseData.currentNewNode.get(finalIndex).previousIndex = data; //new
            traverseData.currentNewNode.get(finalIndex).nextIndex = traverseData.currentNode
                .get(finalIndex).nextIndex; //old
        } else {
            traverseData.currentNewNode.get(finalIndex).nextIndex = data; //new
            traverseData.currentNewNode.get(finalIndex).previousIndex = traverseData.currentNode
                .get(finalIndex).previousIndex; //old
        }

        for (int i = 0; i < branchingFactor; i++) {
            if (i == finalIndex) {
                continue;
            }
            traverseData.currentNewNode.set(i, traverseData.currentNode.get(i));
        }

        return new PersistentLinkedList<>(traverseData.newRoot, branchingFactor, depth, base,
            size, unusedIndices, indexCorrespondingToTheFirstElement,
            indexCorrespondingToTheLatestElement);
    }


    /*public PersistentLinkedList<T> remove(int index) {
        int theIndex = search(index);
        PersistentLinkedList<T> newVersion = removeHelper(index); //TODO: reassign indexCorrespondingToTheFirstElement inside
    }
    public PersistentLinkedList<T> removeFirst() {
        int index = this.indexCorrespondingToTheFirstElement;
        PersistentLinkedList<T> newVersion = removeHelper(index); //TODO: reassign indexCorrespondingToTheFirstElement inside
    }*/

    /**
     * Removes the last element in this list
     *
     * @return new version of the persistent array
     */
    public PersistentLinkedList<T> pop() {
        //the latest element won't become empty
        int index = this.size - 1;
        Node newRoot = new Node();

        Node currentNode = this.root;
        Node currentNewNode = newRoot;

        ArrayList<Node> newNodes = new ArrayList<>();
        newNodes.add(newRoot);
        ArrayList<Integer> newNodesIndices = new ArrayList<>();

        for (int b = base; b > 1; b = b / branchingFactor) {
            TraverseData traverseData = traverseOneLevel(
                new TraverseData(currentNode, currentNewNode, newRoot, index, b));
            currentNode = traverseData.currentNode;
            currentNewNode = traverseData.currentNewNode;
            newNodes.add(currentNewNode);
            newNodesIndices.add(index / b);
            index = traverseData.index;
        }
        newNodesIndices.add(index);

        for (int i = 0; i < branchingFactor && i < index; i++) {
            currentNewNode.set(i, currentNode.get(i));
        }
        currentNewNode.set(index, null);

        if (index == 0) {
            int latestIndex = newNodes.size() - 2;
            newNodes.get(latestIndex).set(newNodesIndices.get(latestIndex), null);

            for (int i = latestIndex; i > 0; i--) {
                if (newNodesIndices.get(i) == 0) {
                    newNodes.get(i - 1).set(newNodesIndices.get(i - 1), null);
                } else {
                    break;
                }
            }
        }

        if (newNodes.size() > 1) {
            int nonNullChildren = 0;
            for (Node child : newRoot.children) {
                if (child != null) {
                    nonNullChildren++;
                }
            }
            if (nonNullChildren == 1) { //need new root
                newRoot = newRoot.get(0);
                return new PersistentLinkedList<>(newRoot, this.branchingFactor, this.depth - 1,
                    this.base / branchingFactor, this.size - 1, unusedIndices,
                    indexCorrespondingToTheFirstElement, indexCorrespondingToTheLatestElement);
            }
        }
        return new PersistentLinkedList<>(newRoot, this.branchingFactor, this.depth, this.base,
            this.size - 1, unusedIndices, indexCorrespondingToTheFirstElement,
            indexCorrespondingToTheLatestElement);
    }

    /**
     * recursive function returning the string representation of the current subgraph
     *
     * @param node root node for the current subgraph
     * @param curDepth depth left till the leaf level
     * @return string representation of the current subgraph
     */
    private String toStringHelper(Node node, int curDepth) {
        if (node.data != null) {
            return node.data.toString();
        }
        StringBuilder outString = new StringBuilder();
        for (int i = 0; i < branchingFactor; i++) {
            if (node.get(i) == null) {
                outString.append("_");
                //break;
            } else {
                if (curDepth == 0) {
                    outString.append(node.get(i).toString());

                } else {
                    outString.append(toStringHelper(node.get(i), curDepth - 1));
                }
            }

            if (i + 1 != branchingFactor) {
                outString.append(", ");
            }
        }
        return "(" + outString + ")";
    }

    public String innerRepresentation() {
        return toStringHelper(this.root, this.depth);
    }

    @Override
    public String toString() {
        StringBuilder outString = new StringBuilder();
        int currentTreeIndex = this.indexCorrespondingToTheFirstElement;
        while (currentTreeIndex != -1) {
            Node currentNode = getHelper(currentTreeIndex);
            outString.append(currentNode.data).append(", ");
            currentTreeIndex = currentNode.nextIndex;
        }

        if (outString.length() != 0) {
            outString.deleteCharAt(outString.length() - 1); // ", "
            outString.deleteCharAt(outString.length() - 1);
        }
        return "[" + outString.toString() + "]";
    }
}
