package q2.a;

import java.util.Arrays;
import java.util.concurrent.atomic.*;
public class PIncrement implements Runnable {


    static int counter;
    static int max = 120000;
    static CLHLock bLock;
    static Thread[] threads;
    static int numProc;

    PIncrement(int c) {
        counter = c;
    }

    public static int parallelIncrement(int c, int numThreads) {
        // your implementation goes here
        PIncrement p = new PIncrement(c);
        bLock = new CLHLock();
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

class CLHLock {
    class Node {
        boolean locked;
    }

    AtomicReference<Node> tail;
    ThreadLocal<Node> node;
    ThreadLocal<Node> pred;

    public CLHLock() {
        tail = new AtomicReference<Node>(new Node());
        tail.get().locked = false;
        node = new ThreadLocal<Node>() {
           @Override protected Node initialValue() {
                return new Node();
            }
        };

        pred = new ThreadLocal<Node>();
    }

    public void lock() {
        node.get().locked = true;
        pred.set(tail.getAndSet(node.get()));
        while(pred.get().locked) {
            Thread.yield();
        }
    }

    public void unlock() {
        node.get().locked = false;
        node.set(pred.get());
    }


}
