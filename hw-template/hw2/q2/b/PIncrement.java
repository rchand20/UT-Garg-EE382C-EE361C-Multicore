package q2.b;

import java.util.Arrays;

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
    int x;
    int y;
    boolean[] flag;

    public LamportLock(int numThreads) {
        x = -1;
        y = -1;
        flag = new boolean[numThreads];
    }

    public void lock(int i) {
        while(true) {
            flag[i] = true;
            x = i;
            if(y != -1) {
                flag[i] = false;
                while(y != -1) {
                    // System.out.println("waiting for y to become -1 in thread " + i);
                }
                continue;
            }
            else {
                y = i;
                if(x == i) return;
                else {
                    flag[i] = false;
                    for(int j = 0; j < flag.length; j++) {
                        if (j != i) {
                            while(flag[j]) {}
                        }
                    }
                    if(y == i) return;
                    else {
                        while (y != -1) {}
                        continue;
                    }
                }
            }
        }
    }

    public void unlock(int i) {
        y = -1;
        flag[i] = false;
    }
}