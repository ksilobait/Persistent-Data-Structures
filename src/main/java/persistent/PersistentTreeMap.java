package persistent;

public class PersistentTreeMap<K, V> {

    final NodeMap<K, V> root;
    final int branchingFactor;
    final int depth;
    final int base; //BF ^ (depth - 1)
    final int size;

    /**
     * package-private constructor for the persistent array
     *
     * @param root a designated/initial vertex in a graph
     * @param branchingFactor number of children at each node
     * @param depth maximum number of edges in the paths from the root to any node
     * @param base branchingFactor ^ (depth - 1)
     * @param size number of leaves in the graph or elements in the persistent tree map
     */
    PersistentTreeMap(NodeMap<K, V> root, int branchingFactor, int depth, int base, int size) {
        this.root = root;
        this.branchingFactor = branchingFactor;
        this.depth = depth;
        this.base = base;
        this.size = size;
    }

    /**
     * constructor for the persistent tree map
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
        this.root = new NodeMap<K, V>(branchingFactor);
        this.depth = 5;
        this.base = (int) Math.pow(branchingFactor, depth - 1);
        this.size = 0;
    }

    private class TraverseData {

        NodeMap<K, V> currentNode;

        NodeMap<K, V> currentNewNode;
        NodeMap<K, V> newRoot;
        int index;
        int base;

        public TraverseData(NodeMap<K, V> currentNode, NodeMap<K, V> currentNewNode,
            NodeMap<K, V> newRoot, int index,
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
        NodeMap<K, V> currentNode = data.currentNode;
        NodeMap<K, V> currentNewNode = data.currentNewNode;
        int nextBranch = data.index / data.base;

        currentNewNode.set(nextBranch, new NodeMap<K, V>(branchingFactor));
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
     * @param index destination index in the tree map
     * @return metadata of traversing
     */
    private TraverseData traverse(int index) {
        NodeMap<K, V> newRoot = new NodeMap<K, V>(branchingFactor);
        NodeMap<K, V> currentNode = this.root;
        NodeMap<K, V> currentNewNode = newRoot;

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
     * Returns the element for the specified key in this tree map
     *
     * @param key key of the element to be returned
     * @return the element for the specified key in the given tree map
     */
    public V get(K key) {
        NodeMap<K, V> currentNode = getHelper(key);
        int position = currentNode.keys.indexOf(key);
        if (position != -1) {
            return (V) currentNode.values.get(position);
        } else {
            return null;
        }
    }

    /**
     * Replaces the element (to be returned) at the specified position in this list with the
     * specified element
     *
     * @param key key of the element to put
     * @param value value of the element to be stored for the specified key
     * @return new version of the persistent tree map
     */
    public PersistentTreeMap<K, V> put(K key, V value) {
        int index = getHash(key);

        TraverseData traverseData = traverse(index);
        NodeMap<K, V> node;
        if (traverseData.currentNode != null) {
            node = new NodeMap<K, V>(traverseData.currentNode.get(traverseData.index), key, value);
        } else {
            node = new NodeMap<K, V>(null, key, value);
        }
        traverseData.currentNewNode.set(traverseData.index, node);
        for (int i = 0; i < branchingFactor; i++) {
            if (i == traverseData.index) {
                continue;
            }
            if (traverseData.currentNode == null) {
                traverseData.currentNewNode.set(i, null);
            } else {
                traverseData.currentNewNode.set(i, traverseData.currentNode.get(i));
            }
        }

        return new PersistentTreeMap<>(traverseData.newRoot, this.branchingFactor, this.depth,
            this.base, this.size + 1);
    }

    /**
     * Removes the last element in this list
     *
     * @return new version of the persistent tree map
     */
    public PersistentTreeMap<K, V> remove(K key) {
        return put(key, null);
    }

    private NodeMap<K, V> getHelper(K key) {
        NodeMap<K, V> currentNode = this.root;
        int index = getHash(key);
        for (int b = base; b > 1; b = b / branchingFactor) {
            int nextBranch = index / b;

            //down
            currentNode = currentNode.get(nextBranch);
            index = index % b;
        }
        return currentNode.get(index);
    }

    public boolean containsKey(K key) {
        NodeMap<K, V> currentNode = getHelper(key);
        int position = currentNode.keys.indexOf(key);
        return position != -1;
    }

    private int getHash(K key) {
        return key.hashCode() % (this.base * this.branchingFactor);
    }

    /**
     * recursive function returning the string representation of the current subgraph
     *
     * @param node root node for the current subgraph
     * @param curDepth depth left till the leaf level
     * @return string representation of the current subgraph
     */
    private String toStringHelper(NodeMap<K, V> node, int curDepth) {
        if (node.keys != null && node.keys.size() != 0) {
            return node.values.toString();
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

    @Override
    public String toString() {
        return toStringHelper(this.root, this.depth);
    }
}

