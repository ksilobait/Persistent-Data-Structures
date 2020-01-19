package persistent;

public class PersistentArray {

    private final Object[] root;
    private final int branchingFactor;
    private final int depth;
    private final int base; //BF ^ (depth - 1)
    private final int size;

    public PersistentArray(Object[] root, int branchingFactor, int depth, int base, int size) {
        this.root = root;
        this.branchingFactor = branchingFactor;
        this.depth = depth;
        this.base = base;
        this.size = size;
    }

    public PersistentArray(Object data, int powerOfBranchingFactor) {
        int branchingFactor = 1;
        for (int i = 0; i < powerOfBranchingFactor; i++) {
            branchingFactor *= 2;
        }

        this.root = new Object[branchingFactor];
        this.root[0] = data;
        this.branchingFactor = branchingFactor;
        this.depth = 1;
        this.base = 1;
        this.size = 1;
    }

    //Replaces the element (to be returned) at the specified position in this list with the specified element
    public PersistentArray set(int index, Object data) {
        int newSize = this.size;
        if (newSize == index) { //when adding to the end ?TODO?
            newSize++;
        }

        Object[] newRoot = new Object[branchingFactor]; //TODO 2 -> BF

        Object[] currentNode = this.root;
        Object[] currentNewNode = newRoot;

        for (int b = base; b > 1; b = b / branchingFactor) { //TODO b=b/2 -> b=b/BF
            int nextBranch = index / b;
            currentNewNode[nextBranch] = new Object[branchingFactor]; //TODO 2 -> BF

            for (int anotherBranch = 0; anotherBranch < branchingFactor; anotherBranch++) {
                if (anotherBranch == nextBranch) {
                    continue;
                }
                currentNewNode[anotherBranch] = currentNode[anotherBranch];
            }

            //down
            currentNode = (Object[]) currentNode[nextBranch];
            currentNewNode = (Object[]) currentNewNode[nextBranch];
            index = index % b;
        }

        currentNewNode[index] = data;
        for (int i = 0; i < branchingFactor; i++) {
            if (i == index) {
                continue;
            }
            currentNewNode[i] = currentNode[i];
        }

        return new PersistentArray(newRoot, this.branchingFactor, this.depth, this.base, newSize);
    }

    public PersistentArray add(Object data) {
        //there's still space in the latest element
        if (this.size % branchingFactor != 0) {
            return set(this.size, data);
        }

        //there's still space for the new data
        if (this.base * branchingFactor > this.size) {
            Object[] newRoot = new Object[branchingFactor];

            Object[] currentNode = this.root;
            Object[] currentNewNode = newRoot;

            int index = this.size;
            int b;
            for (b = base; b > 0; b = b / branchingFactor) {
                int nextBranch = index / b;
                currentNewNode[nextBranch] = new Object[branchingFactor];

                for (int anotherBranch = 0; anotherBranch < branchingFactor; anotherBranch++) {
                    if (anotherBranch == nextBranch) {
                        continue;
                    }
                    currentNewNode[anotherBranch] = currentNode[anotherBranch];
                }

                //down
                currentNewNode = (Object[]) currentNewNode[nextBranch];
                index = index % b;

                if (currentNode[nextBranch] == null) {
                    b = b / branchingFactor;
                    break;
                } else {
                    currentNode = (Object[]) currentNode[nextBranch];
                }
            }
            while (b > 1) {
                currentNewNode[0] = new Object[branchingFactor];
                currentNewNode = (Object[]) currentNewNode[0];
                index = index % b;
                b = b / branchingFactor;
            }
            currentNewNode[0] = data;

            return new PersistentArray(newRoot, this.branchingFactor, this.depth, this.base,
                this.size + 1);
        }

        //root overflow
        Object[] newRoot = new Object[branchingFactor];
        newRoot[0] = this.root;
        newRoot[1] = new Object[branchingFactor];
        //newRoot[2..]=null
        Object[] currentNewNode = (Object[]) newRoot[1];

        int b = base;
        while (b > 1) {
            currentNewNode[0] = new Object[branchingFactor];
            currentNewNode = (Object[]) currentNewNode[0];
            b = b / branchingFactor;
        }
        currentNewNode[0] = data;

        return new PersistentArray(newRoot, this.branchingFactor, this.depth + 1,
            this.base * branchingFactor, this.size + 1);
    }

    public PersistentArray pop() {
        //the latest element won't become empty
        int index = this.size - 1;
        Object[] currentNode = this.root;
        Object[] newRoot = new Object[branchingFactor];
        Object[] currentNewNode = newRoot;

        for (int b = base; b > 1; b = b / branchingFactor) {
            int nextBranch = index / b;
            currentNewNode[nextBranch] = new Object[branchingFactor];

            for (int anotherBranch = 0; anotherBranch < branchingFactor; anotherBranch++) {
                if (anotherBranch == nextBranch) {
                    continue;
                }
                currentNewNode[anotherBranch] = currentNode[anotherBranch];
            }

            //down
            currentNode = (Object[]) currentNode[nextBranch];
            currentNewNode = (Object[]) currentNewNode[nextBranch];
            index = index % b;
        }

        for (int i = 0; i < branchingFactor && i < index; i++) {
            currentNewNode[i] = currentNode[i];
        }
        currentNewNode[index] = null;

        return new PersistentArray(newRoot, this.branchingFactor, this.depth, this.base,
            this.size - 1);
    }

    private String toStringHelper(Object[] node, int curDepth) {
        StringBuilder outString = new StringBuilder();
        for (int i = 0; i < branchingFactor; i++) {
            if (node[i] == null) {
                outString.append("_");
                if (i + 1 != branchingFactor) {
                    outString.append(", ");
                }
                //break;
            } else {
                if (curDepth == 1) {
                    outString.append(node[i].toString());

                } else {
                    outString.append(toStringHelper((Object[]) node[i], curDepth - 1));
                }
                if (i + 1 != branchingFactor) {
                    outString.append(", ");
                }
            }
        }
        return "(" + outString + ")";
    }

    @Override
    public String toString() {
        return toStringHelper(this.root, this.depth);
    }
}
