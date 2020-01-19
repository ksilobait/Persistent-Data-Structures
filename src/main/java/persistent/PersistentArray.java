package persistent;

public class PersistentArray {

    private final Object[] root;
    private final int depth;
    private final int base; //2 ^ (depth - 1)
    private final int size;

    public PersistentArray(Object[] root, int depth, int base, int size) {
        this.root = root;
        this.depth = depth;
        this.base = base;
        this.size = size;
    }

    public PersistentArray(Object data) {
        this.root = new Object[2];
        this.root[0] = data;
        this.depth = 1;
        this.base = 1;
        this.size = 1;
    }
    /*public PersistentArray(ArrayList<Object> initList) {
        Object[] newRoot = new Object[2];
        Object[] currentNode = newRoot;
        int base = 1;
        while (base < initList.size()) {
            base *= 2;
        }
        base /= 2;

    }*/

    //Replaces the element (to be returned) at the specified position in this list with the specified element
    public PersistentArray set(int index, Object data) {
        int newSize = this.size;
        if (newSize == index) { //when adding to the end
            newSize++;
        }

        Object[] currentNode = this.root;
        Object[] newRoot = new Object[2];
        Object[] currentNewNode = newRoot;

        for (int b = base; b > 1; b = b / 2) {
            int nextBranch = index / b;
            int anotherBranch = (nextBranch + 1) % 2;

            currentNewNode[nextBranch] = new Object[2];
            currentNewNode[anotherBranch] = currentNode[anotherBranch];

            currentNode = (Object[]) currentNode[nextBranch];
            currentNewNode = (Object[]) currentNewNode[nextBranch];

            index = index % b;
        }

        int nextBranch = index;
        int anotherBranch = (nextBranch + 1) % 2;
        currentNewNode[nextBranch] = data;
        currentNewNode[anotherBranch] = currentNode[anotherBranch];

        return new PersistentArray(newRoot, this.depth, this.base, newSize);
    }

    public PersistentArray add(Object data) {
        //there's still space in the latest element
        if (this.size % 2 == 1) {
            return set(this.size, data);
        }

        //there's still space for the new data
        if (this.base * 2 < this.size) {
            Object[] currentNode = this.root;
            Object[] newRoot = new Object[2];
            Object[] currentNewNode = newRoot;
            int index = this.size;

            int b;
            for (b = base; b > 0; b = b / 2) {
                int nextBranch = index / b;
                int anotherBranch = (nextBranch + 1) % 2;

                currentNewNode[nextBranch] = new Object[2];
                currentNewNode[anotherBranch] = currentNode[anotherBranch];

                if (currentNode[nextBranch] == null) {
                    currentNewNode = (Object[]) currentNewNode[nextBranch];
                    index = index % b;
                    break;
                }

                currentNode = (Object[]) currentNode[nextBranch];
                currentNewNode = (Object[]) currentNewNode[nextBranch];

                index = index % b;
            }
            while (b > 1) {
                currentNewNode[0] = new Object[2];
                currentNewNode = (Object[]) currentNewNode[0];
                index = index % b;
                b = b / 2;
            }
            currentNewNode[0] = data;

            return new PersistentArray(newRoot, this.depth, this.base, this.size + 1);
        }

        //root overflow
        Object[] newRoot = new Object[2];
        newRoot[0] = this.root;
        newRoot[1] = new Object[2];
        Object[] currentNewNode = (Object[]) newRoot[1];

        int b = base;
        while (b > 1) {
            currentNewNode[0] = new Object[2];
            currentNewNode = (Object[]) currentNewNode[0];
            b = b / 2;
        }
        currentNewNode[0] = data;

        return new PersistentArray(newRoot, this.depth + 1, this.base * 2, this.size + 1);
    }

    private String toStringHelper(Object[] node, int curDepth) {
        if (curDepth == 1) {
            String subString1 = "_";
            if (node[0] != null) {
                subString1 = node[0].toString();
            }
            String subString2 = "_";
            if (node[1] != null) {
                subString2 = node[1].toString();
            }
            return "(" + subString1 + ", " + subString2 + ")";
        }

        String subString1 = "_";
        if (node[0] != null) {
            subString1 = toStringHelper((Object[]) node[0], curDepth - 1);
        }
        String subString2 = "_";
        if (node[1] != null) {
            subString2 = toStringHelper((Object[]) node[1], curDepth - 1);
        }
        return "(" + subString1 + ", " + subString2 + ")";
    }

    @Override
    public String toString() {
        return toStringHelper(this.root, this.depth);
    }
}
