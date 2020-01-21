package persistent;

import java.util.ArrayList;


public class PersistentTreeMap<K, V> {

    final Node root;
    final int branchingFactor;
    final int depth;
    final int base; //BF ^ (depth - 1)
    final int size;

    /**
     * fat node in the graph
     */
    class Node {

        ArrayList<Node> children;
        V data;

        /**
         * constructor for internal (non-leaf) nodes
         */
        Node() {
            this.data = null;
            this.children = new ArrayList<>();
            for (int i = 0; i < branchingFactor; i++) {
                this.children.add(null);
            }
        }

        /**
         * constructor for leaf nodes
         *
         * @param data data to be stored in the leaf
         */
        Node(V data) {
            this.data = data;
            this.children = null;
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
    PersistentTreeMap(Node root, int branchingFactor, int depth, int base, int size) {
        this.root = root;
        this.branchingFactor = branchingFactor;
        this.depth = depth;
        this.base = base;
        this.size = size;
    }

    /**
     * constructor for the persistent array
     *
     * @param powerOfBranchingFactor the branching factor will be equals to
     * 2^powerOfBranchingFactor
     */
    public PersistentTreeMap(int powerOfBranchingFactor) {
        int branchingFactor = 1;
        for (int i = 0; i < powerOfBranchingFactor; i++) {
            branchingFactor *= 2;
        }
        this.branchingFactor = branchingFactor;
        this.root = new Node();
        //this.root.set(index, new Node(data));
        this.depth = 5;
        this.base = (int)Math.pow(branchingFactor, depth - 1);
        this.size = 0;
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
     * traverse one level in the graph
     *
     * @param data metadata before this level
     * @return metadata after this level
     */
    private TraverseData traverseOneLevel(TraverseData data) {
        Node currentNode = data.currentNode;
        Node currentNewNode = data.currentNewNode;
        int nextBranch = data.index / data.base;

        currentNewNode.set(nextBranch, new Node());
        if (currentNode != null) {

            for (int anotherBranch = 0; anotherBranch < branchingFactor; anotherBranch++) {
                if (anotherBranch == nextBranch) {
                    continue;
                }
                currentNewNode.set(anotherBranch, currentNode.get(anotherBranch));
            }
            currentNode = currentNode.get(nextBranch);
        }
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

    /**
     * Returns the element for the specified key in this hash map
     *
     * @param key key of the element to be returned
     * @return the element for the specified key in the given hash map
     */
    public V get(K key) {
        Node currentNode = this.root;
        int index = getHash(key);
        for (int b = base; b > 1; b = b / branchingFactor) {
            int nextBranch = index / b;

            //down
            currentNode = currentNode.get(nextBranch);
            index = index % b;
        }
        if (currentNode.get(index).data == null) {
            return null;
        }
        else {
            return currentNode.get(index).data;
        }

    }

    /**
     * Replaces the element (to be returned) at the specified position in this list with the
     * specified element
     *
     * @param key key of the element to put
     * @param value value of the element to be stored for the specified key
     * @return new version of the persistent array
     */
    public PersistentTreeMap<K, V> put(K key, V value) {
        int index = getHash(key);
        int newSize = this.size;

        TraverseData traverseData = traverse(index);

        traverseData.currentNewNode.set(traverseData.index, new Node(value));
        for (int i = 0; i < branchingFactor; i++) {
            if (i == traverseData.index) {
                continue;
            }
            if(traverseData.currentNode == null) {
                traverseData.currentNewNode.set(i, null);
            }
            else {
                traverseData.currentNewNode.set(i, traverseData.currentNode.get(i));
            }
        }

        return new PersistentTreeMap<>(traverseData.newRoot, this.branchingFactor, this.depth,
                this.base, newSize + 1);
    }

    /**
     * Removes the last element in this list
     *
     * @return new version of the persistent array
     */
    public PersistentTreeMap<K, V> remove(K key) {
        return put(key, null);
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

    private int getHash(K key) {
        return key.hashCode() % (this.base * this.branchingFactor);
    }

    @Override
    public String toString() {
        return toStringHelper(this.root, this.depth);
    }
}

