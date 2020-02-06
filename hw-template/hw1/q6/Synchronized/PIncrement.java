package q6.Synchronized;

import java.util.ArrayList;

public class PIncrement implements Runnable {

    int max = 1200000;
    static Integer counter;
    static Thread[] threads;

    PIncrement(int c) {
        counter = c;
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

        return counter;
    }

    @Override
    public void run() {
        while (counter < max) {
            synchronized (counter) {
                if (counter < max) {
                    counter++;
                }
            }
        }
    }
}
