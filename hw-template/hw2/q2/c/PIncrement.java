package q2.c;

import java.util.concurrent.atomic.AtomicInteger;

public class PIncrement implements Runnable {


    static int counter;
    static int max = 120000;
    static AndersonLock bLock;
    static Thread[] threads;
    static int numProc;

    PIncrement(int c) {
        counter = c;
    }

    public static int parallelIncrement(int c, int numThreads) {
        // your implementation goes here
        PIncrement p = new PIncrement(c);
        bLock = new AndersonLock(numThreads);
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
        // TODO Auto-generated method stub
        //int index = Arrays.asList(threads).indexOf(Thread.currentThread());
        while(counter < max) {
            bLock.lock();
            if(counter < max) {
                counter++;
            }

            bLock.unlock();
        }
        return;
    }

}

class AndersonLock {
    AtomicInteger tail = new AtomicInteger(0);
    boolean[] available;
    int numThreads;
    ThreadLocal<Integer> slot;

    public AndersonLock(int numThreads) {
        available = new boolean[numThreads];
        this.numThreads = numThreads;
        available[0] = true;
        slot = new ThreadLocal<Integer>() {
            @Override
            protected Integer initialValue() {
                return 0;
            }
        };
    }

    public void lock() {
        slot.set(tail.getAndIncrement() % numThreads);
        while(!available[(Integer) slot.get()]);
    }

    public void unlock() {
        available[(Integer) slot.get()] = false;
        available[((Integer) slot.get() + 1) % numThreads] = true;
    }
}
