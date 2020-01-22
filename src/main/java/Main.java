import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.FutureTask;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.TimeUnit;
import persistent.PersistentArray;
import persistent.PersistentTreeMap;

class BubbleSort implements Callable<PersistentArray<Integer>> {

    private PersistentArray<Integer> array;

    public BubbleSort(PersistentArray<Integer> array) {
        this.array = array;
    }

    @Override
    public PersistentArray<Integer> call() {
        int n = array.size();
        Integer temp;
        for (int i = 0; i < n; i++) {
            for (int j = 1; j < (n - i); j++) {
                Integer el_j_1 = array.get(j - 1);
                Integer el_j = array.get(j);
                if (el_j_1 > el_j) {
                    //swap elements
                    temp = el_j_1;
                    array = array.set(j - 1, el_j);
                    array = array.set(j, temp);
                }
            }
        }
        return this.array;
    }
}

class SelectionSort implements Callable<PersistentArray<Integer>> {

    private PersistentArray<Integer> array;

    public SelectionSort(PersistentArray<Integer> array) {
        this.array = array;
    }

    @Override
    public PersistentArray<Integer> call() {
        int n = array.size();

        // One by one move boundary of unsorted subarray
        for (int i = 0; i < n - 1; i++) {
            // Find the minimum element in unsorted array
            int min_idx = i;
            for (int j = i + 1; j < n; j++) {
                if (array.get(j) < array.get(min_idx)) {
                    min_idx = j;
                }
            }

            // Swap the found minimum element with the first element
            int temp = array.get(min_idx);
            array = array.set(min_idx, array.get(i));
            array = array.set(i, temp);
        }
        return this.array;
    }
}

public class Main {

    /*public static void main(String[] args) {
        PersistentArray<Integer> data = new PersistentArray<>(8);
        for (int i = 0; i < 1000; i++) {
            data = data.add(ThreadLocalRandom.current().nextInt(0, 99999));
        }
        ExecutorService executor = Executors.newFixedThreadPool(2);
        FutureTask<PersistentArray<Integer>> f1 = new FutureTask<>(new BubbleSort(data));
        FutureTask<PersistentArray<Integer>> f2 = new FutureTask<>(new SelectionSort(data));
        executor.execute(f1);
        executor.execute(f2);

        while (true) {
            try {
                // if both future task complete
                if (f1.isDone() && f2.isDone()) {
                    System.out.println("Both FutureTask Complete");

                    // shut down executor service
                    executor.shutdown();
                    return;
                }

                if (!f1.isDone()) {

                    // wait indefinitely for future
                    // task to complete
                    System.out.println("FutureTask1 output = " + f1.get());
                }

                System.out.println("Waiting for FutureTask2 to complete");

                // Wait if necessary for the computation to complete and then retrieves its result
                PersistentArray<Integer> s = f2.get(250, TimeUnit.MILLISECONDS);

                if (s != null) {
                    System.out.println("FutureTask2 output=" + s.toString());
                }
            } catch (Exception e) {
                System.out.println(e.getMessage());
            }
        }
    }*/

    public static void main(String[] args) {
        PersistentTreeMap<Integer, String> a = new PersistentTreeMap<>(1);
        PersistentTreeMap<Integer, String> b = a.put(6, "hmm");
        PersistentTreeMap<Integer, String> c = b.put(9999998, "hee");
        PersistentTreeMap<Integer, String> bb = c.remove(9999998);
        PersistentTreeMap<Integer, String> aa = bb.remove(6);
        System.out.println(b);
        System.out.println(bb);
    }

}

