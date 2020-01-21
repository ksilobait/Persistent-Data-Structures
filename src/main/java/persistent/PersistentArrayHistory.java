package persistent;

public class PersistentArrayHistory<T> extends PersistentArray<T> {

    private final PersistentArrayHistory<T> latestVersion;
    private PersistentArrayHistory<T> futureVersion;

    /**
     * constructor for the persistent array
     *
     * @param data initial value for the array
     * @param powerOfBranchingFactor the branching factor will be equals to
     * 2^powerOfBranchingFactor
     */
    public PersistentArrayHistory(T data, int powerOfBranchingFactor) {
        super(data, powerOfBranchingFactor);
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

    /**
     * @return the version that created this one
     */
    public PersistentArrayHistory<T> undo() {
        if (latestVersion == null) {
            return null;
        }
        return latestVersion.setRedo(this);
    }

    /**
     * before undo() sets the version to return to via redo()
     *
     * @param futureVersion the next version to return to via redo()
     * @return the old version with set redo() way back
     */
    private PersistentArrayHistory<T> setRedo(PersistentArrayHistory<T> futureVersion) {
        this.futureVersion = futureVersion;
        return this;
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
