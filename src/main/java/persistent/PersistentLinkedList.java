package persistent;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.SortedSet;
import java.util.TreeSet;

public class PersistentLinkedList<T> {

    final Node root;
    final int branchingFactor;
    final int depth;
    final int base; //BF ^ (depth - 1)
    final int treeSize;
    SortedSet<Integer> unusedTreeIndices = new TreeSet<>();
    int indexCorrespondingToTheFirstElement;
    int indexCorrespondingToTheLatestElement;

    /**
     * fat node in the graph
     */
    class Node {

        ArrayList<Node> children;
        T data;
        int previousIndex; //-1 or non-negative graph index corresponding to the next element in the linked list
        int nextIndex; //-1 or non-negative graph index corresponding to the next element in the linked list

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
     * package-private constructor for the persistent linked list
     *
     * @param root a designated/initial vertex in a graph
     * @param branchingFactor number of children at each node for the underlying structure
     * @param depth maximum number of edges in the paths from the root to any node
     * @param base branchingFactor ^ (depth - 1)
     * @param treeSize number of leaves in the graph or elements with gaps in the persistent array
     * @param unusedTreeIndices indices of the gaps
     * @param indexCorrespondingToTheFirstElement graph index corresponding to the first element in
     * the linked list
     * @param indexCorrespondingToTheLatestElement graph index corresponding to the last element in
     * the linked list
     */
    PersistentLinkedList(Node root, int branchingFactor, int depth, int base, int treeSize,
        SortedSet<Integer> unusedTreeIndices, int indexCorrespondingToTheFirstElement,
        int indexCorrespondingToTheLatestElement) {
        this.root = root;
        this.branchingFactor = branchingFactor;
        this.depth = depth;
        this.base = base;
        this.treeSize = treeSize;
        this.unusedTreeIndices.addAll(unusedTreeIndices);
        this.indexCorrespondingToTheFirstElement = indexCorrespondingToTheFirstElement;
        this.indexCorrespondingToTheLatestElement = indexCorrespondingToTheLatestElement;
    }

    /**
     * constructor for the persistent linked list
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
        this.treeSize = 1;
        this.indexCorrespondingToTheFirstElement = 0;
        this.indexCorrespondingToTheLatestElement = 0;
    }

    /**
     * change (add or remove) (un)used indices in the graph backend [time O(1)]
     *
     * @param treeIndex the index
     * @param removeFromUnused if true then consider "the gap" filled; if false, create a "new gap"
     */
    private void changeUnusedIndices(int treeIndex, boolean removeFromUnused) {
        if (removeFromUnused) {
            this.unusedTreeIndices.remove(treeIndex);
        } else {
            if (treeIndex < this.treeSize) {
                this.unusedTreeIndices.add(treeIndex);
            }
        }
    }

    /**
     * metadata for the graph traversal
     */
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
     * traverse one level in the graph with copying [time O(BF)=O(1)]
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
     * traverse the old structure while creating the new one and copying data into it [time
     * O(log(BF, N))]
     *
     * @param treeIndex destination index in the array
     * @return metadata of traversing
     */
    private TraverseData traverse(int treeIndex) {
        Node newRoot = new Node();
        Node currentNode = this.root;
        Node currentNewNode = newRoot;

        for (int b = base; b > 1; b = b / branchingFactor) {
            TraverseData data = traverseOneLevel(
                new TraverseData(currentNode, currentNewNode, newRoot, treeIndex, b));
            currentNode = data.currentNode;
            currentNewNode = data.currentNewNode;
            treeIndex = data.index;
        }
        return new TraverseData(currentNode, currentNewNode, newRoot, treeIndex, 1);
    }

    /**
     * find index in the graph structure given the index in the linked list  [time O(N * log(BF,
     * N))]
     *
     * @param listIndex index of the element in the linked list
     * @return corresponding index in the graph backend
     */
    private int searchIndex(int listIndex) {
        int currentTreeIndex = this.indexCorrespondingToTheFirstElement;
        Node currentNode = getHelper(currentTreeIndex);
        for (int i = 0; i < listIndex; i++) {
            currentTreeIndex = currentNode.nextIndex;
            currentNode = getHelper(currentTreeIndex);
        }
        return currentTreeIndex;
    }

    /**
     * get ith element in the PersistentArray [time O(log(BF, N))]
     *
     * @param treeIndex index relating to PersistentArray location
     * @return Node representing needed element
     */
    private Node getHelper(int treeIndex) {
        Node currentNode = this.root;

        for (int b = base; b > 1; b = b / branchingFactor) {
            int nextBranch = treeIndex / b;

            //down
            currentNode = currentNode.get(nextBranch);
            treeIndex = treeIndex % b;
        }
        return currentNode.get(treeIndex);
    }

    /**
     * get the first element of the linked list [time O(N * log(BF, N))]
     *
     * @return first element
     */
    public T getFirst() {
        return this.getHelper(this.indexCorrespondingToTheFirstElement).data;
    }

    /**
     * get the last element of the linked list [time O(N * log(BF, N))]
     *
     * @return last element
     */
    public T getLast() {
        return this.getHelper(this.indexCorrespondingToTheLatestElement).data;
    }

    /**
     * get the ith element of the linked list [time O(N * log(BF, N))]
     *
     * @param listIndex index of the element to be returned
     * @return ith element
     */
    public T get(int listIndex) {
        int treeIndex = searchIndex(listIndex);
        return this.getHelper(treeIndex).data;
    }

    /**
     * add the element to the end of the graph [time O(log(BF, N))]
     *
     * @param data the element to be added
     * @return new version of the structure
     */
    private PersistentLinkedList<T> addHelper(T data) {
        //there's still space in the latest element
        if (this.treeSize % branchingFactor != 0) {
            return setHelper(this.treeSize, data);
        }

        //there's still space for the new data
        if (this.base * branchingFactor > this.treeSize) {
            Node newRoot = new Node();

            Node currentNode = this.root;
            Node currentNewNode = newRoot;

            int index = this.treeSize;
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
                this.treeSize + 1, unusedTreeIndices, indexCorrespondingToTheFirstElement,
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
            this.base * branchingFactor, this.treeSize + 1, unusedTreeIndices,
            indexCorrespondingToTheFirstElement, indexCorrespondingToTheLatestElement);
    }

    /**
     * set new value to the given element in the graph [time O(log(BF, N))]
     *
     * @param treeIndex graph index of the element to be changed
     * @param data new value of the element
     * @return new version of the structure
     */
    private PersistentLinkedList<T> setHelper(int treeIndex, T data) {
        int newSize = this.treeSize;
        if (newSize == treeIndex) {
            newSize++;
        }

        TraverseData traverseData = traverse(treeIndex);

        traverseData.currentNewNode.set(traverseData.index, new Node(data));
        for (int i = 0; i < branchingFactor; i++) {
            if (i == traverseData.index) {
                continue;
            }
            traverseData.currentNewNode.set(i, traverseData.currentNode.get(i));
        }

        return new PersistentLinkedList<>(traverseData.newRoot, branchingFactor, depth, base,
            newSize, unusedTreeIndices, indexCorrespondingToTheFirstElement,
            indexCorrespondingToTheLatestElement);
    }

    /**
     * add given element to the ith place of the linked list (current ith element will become i+1)
     * [time O(N * log(BF, N))]
     *
     * @param listIndex index of the element where to insert new data
     * @param data data element to be inserted
     * @return new version of the structure
     */
    public PersistentLinkedList<T> add(int listIndex, T data) {
        int beforeTreeIndex;
        int afterTreeIndex;
        if (listIndex == 0) {
            beforeTreeIndex = -1;
            afterTreeIndex = this.indexCorrespondingToTheFirstElement;
        } else if (listIndex == this.treeSize) {
            beforeTreeIndex = this.indexCorrespondingToTheLatestElement;
            afterTreeIndex = -1;
        } else {
            afterTreeIndex = searchIndex(listIndex);
            beforeTreeIndex = getHelper(afterTreeIndex).previousIndex;
        }

        int newElementTreeIndex;
        PersistentLinkedList<T> newVersion;
        if (unusedTreeIndices.isEmpty()) {
            newElementTreeIndex = this.treeSize;
            newVersion = this.addHelper(data);
        } else {
            newElementTreeIndex = unusedTreeIndices.first();
            newVersion = this.setHelper(newElementTreeIndex, data);
        }
        newVersion.changeUnusedIndices(newElementTreeIndex, true);

        if (beforeTreeIndex != -1) {
            newVersion = newVersion.changeLinks(beforeTreeIndex, newElementTreeIndex);
        } else {
            newVersion.indexCorrespondingToTheFirstElement = newElementTreeIndex;
        }

        if (afterTreeIndex != -1) {
            newVersion = newVersion.changeLinks(newElementTreeIndex, afterTreeIndex);
        } else {
            newVersion.indexCorrespondingToTheLatestElement = newElementTreeIndex;
        }

        return newVersion;
    }

    /**
     * add given element to the beginning of the linked list [time O(log(BF, N))]
     *
     * @param data element to be inserted
     * @return new version of the structure
     */
    public PersistentLinkedList<T> addFirst(T data) {
        return add(0, data);
    }

    /**
     * add given element to the end of the linked list [time O(log(BF, N))]
     *
     * @param data element to be inserted
     * @return new version of the structure
     */
    public PersistentLinkedList<T> addLast(T data) {
        return add(this.treeSize, data);
    }

    /**
     * change links (element order in the linked list) between two nodes in the graph [time
     * O(log(BF, N))]
     *
     * @param treeIndexFrom graph index of preceding element
     * @param treeIndexTo graph index of following element
     * @return new version of the structure
     */
    private PersistentLinkedList<T> changeLinks(int treeIndexFrom, int treeIndexTo) {
        PersistentLinkedList<T> newVersion = this.setLinksHelper(treeIndexFrom, treeIndexTo, false);
        return newVersion.setLinksHelper(treeIndexTo, treeIndexFrom, true);
    }

    /**
     * change links (element order in the linked list) between two nodes in the graph [time
     * O(log(BF, N))]
     *
     * @param treeIndex graph index of the element whose link is going to be changed
     * @param data new value of the link
     * @param setPreviousIndex if true then previousIndex will be changed to given value, nextIndex
     * will be copied
     * @return new version of the structure
     */
    private PersistentLinkedList<T> setLinksHelper(int treeIndex, int data,
        boolean setPreviousIndex) {
        if (treeIndex == -1) {
            return this;
        }

        TraverseData traverseData = traverse(treeIndex);
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
            treeSize, unusedTreeIndices, indexCorrespondingToTheFirstElement,
            indexCorrespondingToTheLatestElement);
    }


    /**
     * Removes the ith element in the linked list [time O(N * log(BF, N))]
     *
     * @param listIndex index of the element in the linked list to be removed
     * @return new version of the structure
     */
    public PersistentLinkedList<T> remove(int listIndex) {
        int treeIndex;
        if (listIndex == 0) {
            treeIndex = this.indexCorrespondingToTheFirstElement;
        } else if (listIndex == this.treeSize - 1) {
            treeIndex = this.indexCorrespondingToTheLatestElement;
        } else {
            treeIndex = searchIndex(listIndex);
        }

        Node toBeRemoved = this.getHelper(treeIndex);
        PersistentLinkedList<T> newVersion = this
            .changeLinks(toBeRemoved.previousIndex, toBeRemoved.nextIndex);
        if (listIndex == 0) {
            newVersion.indexCorrespondingToTheFirstElement = toBeRemoved.nextIndex;
        } else if (listIndex == this.treeSize - 1) {
            newVersion.indexCorrespondingToTheLatestElement = toBeRemoved.previousIndex;
        }

        if (treeIndex == this.treeSize - 1) {
            newVersion = newVersion.pop();
            while (newVersion.unusedTreeIndices.contains(newVersion.treeSize - 1)) {
                newVersion.changeUnusedIndices(newVersion.treeSize - 1, true);
                newVersion = newVersion.pop();
            }
            return newVersion;
        } //else

        PersistentLinkedList<T> newVersion2 = newVersion.setHelper(treeIndex, null);
        newVersion2.changeUnusedIndices(treeIndex, false);
        return newVersion2;
    }

    /**
     * Removes the first element in the linked list [time O(log(BF, N))]
     *
     * @return new version of the structure
     */
    public PersistentLinkedList<T> removeFirst() {
        return remove(0);
    }

    /**
     * Removes the last element in the linked list [time O(log(BF, N))]
     *
     * @return new version of the structure
     */
    public PersistentLinkedList<T> removeLast() {
        return remove(this.treeSize - 1);
    }

    /**
     * Removes the last element in the graph [time O(log(BF, N))]
     *
     * @return new version of the structure
     */
    private PersistentLinkedList<T> pop() {
        //the latest element won't become empty
        int index = this.treeSize - 1;
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
                    this.base / branchingFactor, this.treeSize - 1, unusedTreeIndices,
                    indexCorrespondingToTheFirstElement, indexCorrespondingToTheLatestElement);
            }
        }
        return new PersistentLinkedList<>(newRoot, this.branchingFactor, this.depth, this.base,
            this.treeSize - 1, unusedTreeIndices, indexCorrespondingToTheFirstElement,
            indexCorrespondingToTheLatestElement);
    }

    /**
     * recursive function returning the string representation of the current subgraph [time O(N *
     * log(BF * N))]
     *
     * @param node root node for the current subgraph
     * @param curDepth depth left till the leaf level
     * @return string representation of the current subgraph
     */
    private String toStringHelper(Node node, int curDepth) {
        if (node.data != null) {
            return node.data.toString();
        }
        if (node.children == null) {
            return "_";
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

    /**
     * show toString() using the PersistentArray (graph) backend representation [time O(N * log(BF *
     * N))]
     *
     * @return PersistentArray string representation
     */
    public String innerRepresentation() {
        return toStringHelper(this.root, this.depth);
    }

    /**
     * show LinkedList representation [time O(N * log(BF, N))]
     *
     * @return LinkedList representation
     */
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

    /**
     * convert PersistentLinkedList to LinkedList [time O(N * log(BF, N))]
     *
     * @return LinkedList presentation
     */
    public LinkedList<T> toLinkedList() {
        LinkedList<T> out = new LinkedList<>();
        int currentTreeIndex = this.indexCorrespondingToTheFirstElement;
        while (currentTreeIndex != -1) {
            Node currentNode = getHelper(currentTreeIndex);
            out.add(currentNode.data);
            currentTreeIndex = currentNode.nextIndex;
        }
        return out;
    }

    /**
     * return amount of the elements in the linked list [time O(1)]
     *
     * @return amount of the elements in the linked list
     */
    public int size() {
        return this.treeSize - this.unusedTreeIndices.size();
    }
}
