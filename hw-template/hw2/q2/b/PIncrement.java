package q2.b;

import java.util.Arrays;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

public class PIncrement implements Runnable {

    static int counter;
    static int max = 120000;
    static LamportLock myLock;
    static Thread[] threads;
    static int numProc;

    PIncrement(int c) {
        counter = c;
    }

    public static int parallelIncrement(int c, int numThreads) {
        // your implementation goes here
        PIncrement p = new PIncrement(c);
        myLock = new LamportLock(numThreads);
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
        int index = Arrays.asList(threads).indexOf(Thread.currentThread());
        while(counter < max) {
            myLock.lock(index);
            //System.out.println("Locking thread " + index);
            if(counter < max) {
                counter++;
                //System.out.println(counter);
            }

            myLock.unlock(index);
            //System.out.println("Unlocked thread " + index);
        }
        return;
    }
}

class LamportLock {
    AtomicInteger x;
    AtomicInteger y;
    AtomicBoolean[] flag;

    public LamportLock(int numThreads) {
        x = new AtomicInteger(-1);
        y = new AtomicInteger(-1);
        flag = new AtomicBoolean[numThreads];
        Arrays.fill(flag, new AtomicBoolean());
    }

    public void lock(int i) {
        while(true) {
            flag[i].set(true);
            x.set(i);
            if(y.get() !=-1) {
                flag[i].set(false);
                while(y.get() != -1) {
            
                }
                continue;
            }
            else {
                y.set(i);
                if(x.get() == i) return;
                else {
                    flag[i].set(false);
                    for(int j = 0; j < flag.length; j++) {
                        if (j != i) {
                            while(flag[j].get()) {}
                        }
                    }
                    if(y.get() == i) return;
                    else {
                        while (y.get() != -1) {}
                        continue;
                    }
                }
            }
        }
    }

    public void unlock(int i) {
        y.set(-1);
        flag[i].set(false);
    }
}