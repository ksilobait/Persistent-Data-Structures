package persistent;

import java.util.SortedSet;

public class PersistentLinkedListHistory<T> extends PersistentLinkedList<T> {

    private final PersistentLinkedListHistory<T> latestVersion;
    private PersistentLinkedListHistory<T> futureVersion;

    public PersistentLinkedListHistory(int powerOfBranchingFactor) {
        super(powerOfBranchingFactor);
        this.latestVersion = null;
        this.futureVersion = null;
    }

    private PersistentLinkedListHistory(Node<T> root, int branchingFactor, int depth, int base,
        int treeSize,
        SortedSet<Integer> unusedTreeIndices, int indexCorrespondingToTheFirstElement,
        int indexCorrespondingToTheLatestElement, PersistentLinkedListHistory<T> latestVersion) {
        super(root, branchingFactor, depth, base, treeSize, unusedTreeIndices,
            indexCorrespondingToTheFirstElement, indexCorrespondingToTheLatestElement);
        this.latestVersion = latestVersion;
    }

    private PersistentLinkedListHistory(PersistentLinkedListHistory<T> thisVersion,
        PersistentLinkedListHistory<T> futureVersion) {
        super(thisVersion.root, thisVersion.branchingFactor, thisVersion.depth, thisVersion.base,
            thisVersion.treeSize, thisVersion.unusedTreeIndices, thisVersion.indexCorrespondingToTheFirstElement,
            thisVersion.indexCorrespondingToTheLatestElement);
        this.latestVersion = thisVersion.latestVersion;
        this.futureVersion = futureVersion;
    }

    /**
     * @return the version that created this one
     */
    public PersistentLinkedListHistory<T> undo() {
        if (latestVersion == null) {
            return null;
        }
        return new PersistentLinkedListHistory<T>(latestVersion, this);
    }

    /**
     * undo the undo() operation
     *
     * @return the version that produced this one via undo()
     */
    public PersistentLinkedListHistory<T> redo() {
        return futureVersion;
    }

    @Override
    public PersistentLinkedListHistory<T> add(int listIndex, T data) {
        PersistentLinkedList<T> result = super.add(listIndex, data);
        return new PersistentLinkedListHistory<>(result.root,
            result.branchingFactor, result.depth, result.base, result.treeSize,
            result.unusedTreeIndices,
            result.indexCorrespondingToTheFirstElement, result.indexCorrespondingToTheLatestElement,
            this);
    }

    @Override
    public PersistentLinkedListHistory<T> addFirst(T data) {
        PersistentLinkedList<T> result = super.addFirst(data);
        return new PersistentLinkedListHistory<>(result.root,
            result.branchingFactor, result.depth, result.base, result.treeSize,
            result.unusedTreeIndices,
            result.indexCorrespondingToTheFirstElement, result.indexCorrespondingToTheLatestElement,
            this);
    }

    @Override
    public PersistentLinkedListHistory<T> addLast(T data) {
        PersistentLinkedList<T> result = super.addLast(data);
        return new PersistentLinkedListHistory<>(result.root,
            result.branchingFactor, result.depth, result.base, result.treeSize,
            result.unusedTreeIndices,
            result.indexCorrespondingToTheFirstElement, result.indexCorrespondingToTheLatestElement,
            this);
    }

    @Override
    public PersistentLinkedListHistory<T> remove(int listIndex) {
        PersistentLinkedList<T> result = super.remove(listIndex);
        return new PersistentLinkedListHistory<>(result.root,
            result.branchingFactor, result.depth, result.base, result.treeSize,
            result.unusedTreeIndices,
            result.indexCorrespondingToTheFirstElement, result.indexCorrespondingToTheLatestElement,
            this);
    }

    @Override
    public PersistentLinkedListHistory<T> removeFirst() {
        PersistentLinkedList<T> result = super.removeFirst();
        return new PersistentLinkedListHistory<>(result.root,
            result.branchingFactor, result.depth, result.base, result.treeSize,
            result.unusedTreeIndices,
            result.indexCorrespondingToTheFirstElement, result.indexCorrespondingToTheLatestElement,
            this);
    }

    @Override
    public PersistentLinkedListHistory<T> removeLast() {
        PersistentLinkedList<T> result = super.removeLast();
        return new PersistentLinkedListHistory<>(result.root,
            result.branchingFactor, result.depth, result.base, result.treeSize,
            result.unusedTreeIndices,
            result.indexCorrespondingToTheFirstElement, result.indexCorrespondingToTheLatestElement,
            this);
    }
}
