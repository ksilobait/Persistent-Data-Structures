package persistent;

import java.util.ArrayList;

public class PersistentArray<T> {

    private final Node root;
    private final int branchingFactor;
    private final int depth;
    private final int base; //BF ^ (depth - 1)
    private final int size;

    private class Node {

        ArrayList<Node> children;
        T data;

        Node() {
            this.data = null;
            this.children = new ArrayList<>();
            for (int i = 0; i < branchingFactor; i++) {
                this.children.add(null);
            }
        }

        Node(T data) {
            this.data = data;
            this.children = null;
        }

        Node get(int i) {
            if (this.children.size() <= i) {
                return null;
            }
            return this.children.get(i);
        }

        void set(int i, Node e) {
            /*if (this.children.size() <= i) {
                this.children.add(e);
            } else {*/
            this.children.set(i, e);
            // }
        }
    }

    private PersistentArray(Node root, int branchingFactor, int depth, int base, int size) {
        this.root = root;
        this.branchingFactor = branchingFactor;
        this.depth = depth;
        this.base = base;
        this.size = size;
    }

    public PersistentArray(T data, int powerOfBranchingFactor) {
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

    // traverse old structure while creating new and copying data into it
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

    //Replaces the element (to be returned) at the specified position in this list with the specified element
    public PersistentArray<T> set(int index, T data) {
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

        return new PersistentArray<>(traverseData.newRoot, this.branchingFactor, this.depth,
            this.base, newSize);
    }

    public PersistentArray<T> add(T data) {
        //there's still space in the latest element
        if (this.size % branchingFactor != 0) {
            return set(this.size, data);
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

            return new PersistentArray<>(newRoot, this.branchingFactor, this.depth, this.base,
                this.size + 1);
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

        return new PersistentArray<>(newRoot, this.branchingFactor, this.depth + 1,
            this.base * branchingFactor, this.size + 1);
    }

    public PersistentArray<T> pop() {
        //the latest element won't become empty
        int index = this.size - 1;
        TraverseData traverseData = traverse(index);
        for (int i = 0; i < branchingFactor && i < index; i++) {
            traverseData.currentNewNode.set(i, traverseData.currentNode.get(i));
        }
        traverseData.currentNewNode.set(index, null);
        return new PersistentArray<>(traverseData.newRoot, this.branchingFactor, this.depth,
            this.base,
            this.size - 1);
    }

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

    @Override
    public String toString() {
        return toStringHelper(this.root, this.depth);
    }
}
