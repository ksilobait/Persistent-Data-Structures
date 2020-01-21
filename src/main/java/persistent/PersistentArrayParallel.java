package persistent;

import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Function;

/**
 * thread-safe, has one version of the array for all sharing threads
 *
 * @param <T> type of data to be stored in the array
 */
public class PersistentArrayParallel<T> {

    private AtomicReference<PersistentArray<T>> state = new AtomicReference<>();

    public PersistentArrayParallel(int powerOfBranchingFactor) {
        PersistentArray<T> init = new PersistentArray<T>(powerOfBranchingFactor);
        state.set(init);
    }

    /**
     * Returns the element at the specified position in this list
     *
     * @param index index of the element to be returned
     * @return the element at the specified index in the given list
     */
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

    /**
     * gets element from one place, applies the function to that element and sets new element to
     * another place
     *
     * @param getByIndex index of the element to be passed to function
     * @param setByIndex index of the element to be set as function result
     * @param function transform an element with given in the function rule
     */
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

    /**
     * Replaces the element at the specified position in this list with the specified element
     *
     * @param index index of the element to replace
     * @param data element to be stored at the specified position
     */
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

    /**
     * gets the latest element, applies the function to that element and add new element to the
     * ending
     *
     * @param function transform an element with given in the function rule
     */
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

    /**
     * gets the element by given index, applies the function to that element and add new element to
     * the ending
     *
     * @param getByIndex index of the element to be passed to function
     * @param function transform an element with given in the function rule
     */
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

    /**
     * Append a specified element to the end of a list
     *
     * @param data The element to be appended to this list
     */
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

    /**
     * Removes the last element in this list
     */
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

    /**
     * convert the structure to PersistentLinkedList sharing the same data
     *
     * @return PersistentLinkedList
     */
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

    /**
     * @return persistent array size
     */
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
