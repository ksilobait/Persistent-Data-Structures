package persistent;

public class PersistentArrayHistory<T> extends PersistentArray<T> {

    private final PersistentArrayHistory<T> latestVersion;
    private PersistentArrayHistory<T> futureVersion;

    /**
     * constructor for the persistent array
     *
     * @param powerOfBranchingFactor the branching factor will be equals to
     * 2^powerOfBranchingFactor
     */
    public PersistentArrayHistory(int powerOfBranchingFactor) {
        super(powerOfBranchingFactor);
        this.latestVersion = null;
        this.futureVersion = null;
    }

    /**
     * private constructor for the persistent array
     *
     * @param root a designated/initial vertex in a graph
     * @param branchingFactor number of children at each node
     * @param depth maximum number of edges in the paths from the root to any node
     * @param base branchingFactor ^ (depth - 1)
     * @param size number of leaves in the graph or elements in the persistent array
     * @param latestVersion version to undo to
     */
    private PersistentArrayHistory(Node<T> root, int branchingFactor, int depth, int base, int size,
        PersistentArrayHistory<T> latestVersion) {
        super(root, branchingFactor, depth, base, size);
        this.latestVersion = latestVersion;
    }

    private PersistentArrayHistory(PersistentArrayHistory<T> thisVersion, PersistentArrayHistory<T> futureVersion) {
        super(thisVersion.root, thisVersion.branchingFactor, thisVersion.depth, thisVersion.base, thisVersion.size);
        this.latestVersion = thisVersion.latestVersion;
        this.futureVersion = futureVersion;
    }

    /**
     * @return the version that created this one
     */
    public PersistentArrayHistory<T> undo() {
        if (latestVersion == null) {
            return null;
        }
        return new PersistentArrayHistory<T>(latestVersion, this);
    }

    /**
     * undo the undo() operation
     *
     * @return the version that produced this one via undo()
     */
    public PersistentArrayHistory<T> redo() {
        return futureVersion;
    }

    @Override
    public PersistentArrayHistory<T> set(int index, T data) {
        PersistentArray<T> result = super.set(index, data);
        return new PersistentArrayHistory<>(result.root,
            result.branchingFactor, result.depth, result.base, result.size, this);
    }

    @Override
    public PersistentArrayHistory<T> add(T data) {
        PersistentArray<T> result = super.add(data);
        return new PersistentArrayHistory<>(result.root,
            result.branchingFactor, result.depth, result.base, result.size, this);

    }

    @Override
    public PersistentArrayHistory<T> pop() {
        PersistentArray<T> result = super.pop();
        return new PersistentArrayHistory<>(result.root,
            result.branchingFactor, result.depth, result.base, result.size, this);

    }
}
