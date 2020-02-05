package q6.Bakery;

import java.util.ArrayList;

public class PIncrement implements Runnable {

    static int counter;
    static int max = 1200000;
    static BakeryLock bLock;
    static ArrayList<Thread> threads;
    static int numProc;

    PIncrement(int c) {
        counter = c;
    }

    public static int parallelIncrement(int c, int numThreads) {
        // your implementation goes here
        PIncrement p = new PIncrement(c);
        bLock = new BakeryLock(numThreads);
        threads = new ArrayList<>();

        try {
            for (int i = 0; i < numThreads; i++) {
                Thread t = new Thread(p);
                threads.add(t);
                threads.get(i).start();
            }

            for (int i = 0; i < numThreads; i++) {
                threads.get(i).join();
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
        int index = threads.indexOf(Thread.currentThread());
        while(counter < max) {
            bLock.lock(index);
            if(counter < max) {
                counter++;
            }

            bLock.unlock(index);
        }
        return;
    }
}
