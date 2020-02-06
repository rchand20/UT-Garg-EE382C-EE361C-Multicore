package q6.ReentrantLock;

import java.util.concurrent.locks.ReentrantLock;

public class PIncrement implements Runnable {
    static int counter;
    static int max = 1200000;
    static ReentrantLock bLock;
    static Thread[] threads;

    PIncrement(int c) {
        counter = c;
    }

    public static int parallelIncrement(int c, int numThreads) {
        // your implementation goes here
        PIncrement p = new PIncrement(c);
        threads = new Thread[numThreads];
        bLock = new ReentrantLock();
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
        // TODO Auto-generated method stubw
        while (counter < max) {
            bLock.lock();
            try {
                if (counter < max) {
                    counter++;
                }
            } finally {
                bLock.unlock();
            }
        }
        return;
    }
}
