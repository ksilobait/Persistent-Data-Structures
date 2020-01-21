package persistent;

import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Function;

public class PersistentArrayParallel<T> {
    private AtomicReference<PersistentArray<T>> state = new AtomicReference<>();

    public PersistentArrayParallel(T data, int powerOfBranchingFactor) {
        PersistentArray<T> init = new PersistentArray<T>(data, powerOfBranchingFactor);
        state.set(init);
    }

    public T get(int index) {
        PersistentArray<T> curVersion;
        T value;
        boolean success;
        do {
            curVersion = state.get();
            value = curVersion.get(index);
            success = state.compareAndSet(curVersion, curVersion);
        } while (!success);
        return value;
    }

    public void getAndSet(int getByIndex, int setByIndex, Function<T, T> function) {
        PersistentArray<T> curVersion;
        PersistentArray<T> newVersion;
        boolean success;
        do {
            curVersion = state.get();
            newVersion = curVersion.set(setByIndex, function.apply(curVersion.get(getByIndex)));
            success = state.compareAndSet(curVersion, newVersion);
        } while (!success);
    }


    public void set(int index, T data) {
        PersistentArray<T> curVersion;
        PersistentArray<T> newVersion;
        boolean success;
        do {
            curVersion = state.get();
            newVersion = curVersion.set(index, data);
            success = state.compareAndSet(curVersion, newVersion);
        } while (!success);
    }

    public void getLastAndAdd(Function<T, T> function) {
        PersistentArray<T> curVersion;
        PersistentArray<T> newVersion;
        boolean success;
        do {
            curVersion = state.get();
            newVersion = curVersion.add(function.apply(curVersion.get(curVersion.size - 1)));
            success = state.compareAndSet(curVersion, newVersion);
        } while (!success);
    }


    public void getAndAdd(int getByIndex, Function<T, T> function) {
        PersistentArray<T> curVersion;
        PersistentArray<T> newVersion;
        boolean success;
        do {
            curVersion = state.get();
            newVersion = curVersion.add(function.apply(curVersion.get(getByIndex)));
            success = state.compareAndSet(curVersion, newVersion);
        } while (!success);
    }


    public void add(T data) {
        PersistentArray<T> curVersion;
        PersistentArray<T> newVersion;
        boolean success;
        do {
            curVersion = state.get();
            newVersion = curVersion.add(data);
            success = state.compareAndSet(curVersion, newVersion);
        } while (!success);
    }

    public void pop() {
        PersistentArray<T> curVersion;
        PersistentArray<T> newVersion;
        boolean success;
        do {
            curVersion = state.get();
            newVersion = curVersion.pop();
            success = state.compareAndSet(curVersion, newVersion);
        } while (!success);
    }

    public PersistentLinkedList<T> toPersistentLinkedList() {
        PersistentArray<T> curVersion;
        PersistentLinkedList<T> value;
        boolean success;
        do {
            curVersion = state.get();
            value = curVersion.toPersistentLinkedList();
            success = state.compareAndSet(curVersion, curVersion);
        } while (!success);
        return value;
    }

    public int size() {
        PersistentArray<T> curVersion;
        int value;
        boolean success;
        do {
            curVersion = state.get();
            value = curVersion.size();
            success = state.compareAndSet(curVersion, curVersion);
        } while (!success);
        return value;
    }

    @Override
    public String toString() {
        PersistentArray<T> curVersion;
        String value;
        boolean success;
        do {
            curVersion = state.get();
            value = curVersion.toString();
            success = state.compareAndSet(curVersion, curVersion);
        } while (!success);
        return value;
    }
}
