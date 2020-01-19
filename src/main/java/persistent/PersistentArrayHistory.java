package persistent;

public class PersistentArrayHistory<T> extends PersistentArray<T> {

    private final PersistentArrayHistory<T> latestVersion;
    private PersistentArrayHistory<T> futureVersion;

    public PersistentArrayHistory(T data, int powerOfBranchingFactor) {
        super(data, powerOfBranchingFactor);
        this.latestVersion = null;
        this.futureVersion = null;
    }

    private PersistentArrayHistory(Node root, int branchingFactor, int depth, int base, int size,
        PersistentArrayHistory<T> latestVersion) {
        super(root, branchingFactor, depth, base, size);
        this.latestVersion = latestVersion;
    }

    public PersistentArrayHistory<T> undo() {
        if (latestVersion == null) {
            return null;
        }
        return latestVersion.setRedo(this);
    }

    private PersistentArrayHistory<T> setRedo(PersistentArrayHistory<T> futureVersion) {
        this.futureVersion = futureVersion;
        return this;
    }

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
