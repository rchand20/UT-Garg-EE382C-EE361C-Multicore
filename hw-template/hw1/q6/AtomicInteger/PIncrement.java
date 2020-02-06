package q6.AtomicInteger;

import java.util.ArrayList;
import java.util.concurrent.atomic.AtomicInteger;

public class PIncrement implements Runnable {

    int max = 1200000;
    static AtomicInteger counter;
    static Thread[] threads;

    PIncrement(int c) {
        counter = new AtomicInteger(c);
    }

    public static int parallelIncrement(int c, int numThreads) {
        // your implementation goes here
        PIncrement p = new PIncrement(c);
        threads = new Thread[numThreads];
        try {
            for (int i = 0; i < numThreads; i++) {
                // Thread t = new Thread(p);
                threads[i] = new Thread(p);
                threads[i].start();
            }

            for (int i = 0; i < numThreads; i++) {
                threads[i].join();
            }
        }

        catch (Exception e) {
            System.out.println(e);
        }

        return counter.get();
    }

    @Override
    public void run() {
        int current = counter.get();
        while (current < max) {
            int update = current + 1;
            counter.compareAndSet(current, update);
            current = counter.get();
        }
        return;
    }
}
