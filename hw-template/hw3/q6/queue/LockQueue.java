package q6.queue;

import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.locks.ReentrantLock;

public class LockQueue implements MyQueue {
    // you are free to add members
    ReentrantLock enqLock;
    ReentrantLock deqLock;

    Node head;
    Node tail;

    AtomicInteger size;

    public LockQueue() {
        // implement your constructor here
        head = new Node(null);
        tail = head;
        enqLock = new ReentrantLock();
        deqLock = new ReentrantLock();

    }


    public boolean enq(Integer value) {
        // implement your enq method here
        if(value == null) {
            return false;
        }
        enqLock.lock();
        try {
            Node e = new Node(value);
            tail.next = e;
            tail = e;
            return true;
        } finally {
            enqLock.unlock();
        }

    }

    public Integer deq() {
        // implement your deq method here
        Integer result;
        deqLock.lock();

        try {
            if(head.next == null) {
                return null;
            }

            result = head.next.value;
            head = head.next;
            return result;
        } finally {
            deqLock.unlock();
        }
    }

    protected class Node {
        public Integer value;
        public Node next;

        public Node(Integer x) {
            value = x;
            next = null;
        }
    }
}
