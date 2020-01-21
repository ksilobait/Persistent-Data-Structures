package persistent;

import java.util.LinkedList;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Function;

public class PersistentLinkedListParallel<T> {
    private AtomicReference<PersistentLinkedList<T>> state = new AtomicReference<>();

    public PersistentLinkedListParallel(int powerOfBranchingFactor) {
        PersistentLinkedList<T> init = new PersistentLinkedList<T>(powerOfBranchingFactor);
        state.set(init);
    }

    public T getFirst() {
        PersistentLinkedList<T> curVersion;
        T value;
        boolean success;
        do {
            curVersion = state.get();
            value = curVersion.getFirst();
            success = state.compareAndSet(curVersion, curVersion);
        } while (!success);
        return value;
    }

    public T getLast() {
        PersistentLinkedList<T> curVersion;
        T value;
        boolean success;
        do {
            curVersion = state.get();
            value = curVersion.getLast();
            success = state.compareAndSet(curVersion, curVersion);
        } while (!success);
        return value;

    }

    public T get(int listIndex) {
        PersistentLinkedList<T> curVersion;
        T value;
        boolean success;
        do {
            curVersion = state.get();
            value = curVersion.get(listIndex);
            success = state.compareAndSet(curVersion, curVersion);
        } while (!success);
        return value;
    }

    public void add(int listIndex, T data) {
        PersistentLinkedList<T> curVersion;
        PersistentLinkedList<T> newVersion;
        boolean success;
        do {
            curVersion = state.get();
            newVersion = curVersion.add(listIndex, data);
            success = state.compareAndSet(curVersion, newVersion);
        } while (!success);
    }

    public void addFirst(T data) {
        PersistentLinkedList<T> curVersion;
        PersistentLinkedList<T> newVersion;
        boolean success;
        do {
            curVersion = state.get();
            newVersion = curVersion.addFirst(data);
            success = state.compareAndSet(curVersion, newVersion);
        } while (!success);

    }

    public void addLast(T data) {
        PersistentLinkedList<T> curVersion;
        PersistentLinkedList<T> newVersion;
        boolean success;
        do {
            curVersion = state.get();
            newVersion = curVersion.addLast(data);
            success = state.compareAndSet(curVersion, newVersion);
        } while (!success);

    }

    public void remove(int listIndex) {
        PersistentLinkedList<T> curVersion;
        PersistentLinkedList<T> newVersion;
        boolean success;
        do {
            curVersion = state.get();
            newVersion = curVersion.remove(listIndex);
            success = state.compareAndSet(curVersion, newVersion);
        } while (!success);

    }

    public void removeFirst() {
        PersistentLinkedList<T> curVersion;
        PersistentLinkedList<T> newVersion;
        boolean success;
        do {
            curVersion = state.get();
            newVersion = curVersion.removeFirst();
            success = state.compareAndSet(curVersion, newVersion);
        } while (!success);

    }

    public void removeLast() {
        PersistentLinkedList<T> curVersion;
        PersistentLinkedList<T> newVersion;
        boolean success;
        do {
            curVersion = state.get();
            newVersion = curVersion.removeLast();
            success = state.compareAndSet(curVersion, newVersion);
        } while (!success);
    }

    public PersistentArray<T> toPersistentArray() {
        PersistentLinkedList<T> curVersion;
        PersistentArray<T> value;
        boolean success;
        do {
            curVersion = state.get();
            value = curVersion.toPersistentArray();
            success = state.compareAndSet(curVersion, curVersion);
        } while (!success);
        return value;
    }

    public LinkedList<T> toLinkedList() {
        PersistentLinkedList<T> curVersion;
        LinkedList<T> value;
        boolean success;
        do {
            curVersion = state.get();
            value = curVersion.toLinkedList();
            success = state.compareAndSet(curVersion, curVersion);
        } while (!success);
        return value;
    }

    public int size() {
        PersistentLinkedList<T> curVersion;
        int value;
        boolean success;
        do {
            curVersion = state.get();
            value = curVersion.size();
            success = state.compareAndSet(curVersion, curVersion);
        } while (!success);
        return value;
    }

    public String innerRepresentation() {
        PersistentLinkedList<T> curVersion;
        String value;
        boolean success;
        do {
            curVersion = state.get();
            value = curVersion.innerRepresentation();
            success = state.compareAndSet(curVersion, curVersion);
        } while (!success);
        return value;
    }

    public String toString() {
        PersistentLinkedList<T> curVersion;
        String value;
        boolean success;
        do {
            curVersion = state.get();
            value = curVersion.toString();
            success = state.compareAndSet(curVersion, curVersion);
        } while (!success);
        return value;
    }

    public void getLastAndAddLast(Function<T, T> function) {
        PersistentLinkedList<T> curVersion;
        PersistentLinkedList<T> newVersion;
        boolean success;
        do {
            curVersion = state.get();
            newVersion = curVersion.addLast(function.apply(curVersion.getLast()));
            success = state.compareAndSet(curVersion, newVersion);
        } while (!success);
    }
}
