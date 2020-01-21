package persistent;

import static org.junit.Assert.*;

import java.util.concurrent.ThreadLocalRandom;
import java.util.function.Function;
import org.junit.Test;

public class PersistentArrayParallelTest {
    private final int positive = 10;
    private final int negative = 1;

    class T1 implements Runnable {

        private PersistentArrayParallel<Integer> data;

        public T1(PersistentArrayParallel<Integer> data) {
            this.data = data;
        }

        @Override
        public void run() {
            for (int i = 0; i < 100; i++) {
                Function<Integer, Integer> function = t -> t + positive;
                data.getLastAndAdd(function);

                long INTERVAL = ThreadLocalRandom.current().nextInt(0, 100);
                long start = System.nanoTime();
                long end = 0;
                do {
                    end = System.nanoTime();
                } while (start + INTERVAL >= end);
            }
        }
    }

    class T2 implements Runnable {

        private PersistentArrayParallel<Integer> data;

        public T2(PersistentArrayParallel<Integer> data) {
            this.data = data;
        }

        @Override
        public void run() {
            for (int i = 0; i < 100; i++) {
                Function<Integer, Integer> function = t -> t - negative;
                data.getLastAndAdd(function);

                long INTERVAL = ThreadLocalRandom.current().nextInt(0, 100);
                long start = System.nanoTime();
                long end = 0;
                do {
                    end = System.nanoTime();
                } while (start + INTERVAL >= end);
            }
        }
    }

    @Test
    public void parallelTest() throws InterruptedException {
        PersistentArrayParallel<Integer> data = new PersistentArrayParallel<>(100, 2);
        T1 t1 = new T1(data);
        T2 t2 = new T2(data);
        Thread thread1 = new Thread(t1);
        Thread thread2 = new Thread(t2);
        thread1.start();
        thread2.start();
        thread1.join();
        thread2.join();

        int prevElement = data.get(0);
        for (int i = 1; i < data.size(); i++) {
            int curElement = data.get(i);
            int difference = Math.abs(curElement - prevElement);
            prevElement = curElement;
            assertTrue(difference == positive || difference == negative);
        }
    }

}
