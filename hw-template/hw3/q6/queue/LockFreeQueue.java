package q6.queue;

import java.util.concurrent.atomic.AtomicReference;
import java.util.concurrent.atomic.AtomicStampedReference;

public class LockFreeQueue implements MyQueue {
    // you are free to add members
    AtomicReference<Node> head;
    AtomicReference<Node> tail;

    public LockFreeQueue() {
        // implement your constructor here
        Node node = new Node(null);
        head.set(node);
        tail.set(node);
    }

    public boolean enq(Integer value) {
        // implement your enq method here
        Node node = new Node(value);
        while(true) {
            Node tempTail = tail.get();
            Node next = tempTail.next.get();
            if(next == null) {
                if(tail.compareAndSet(tempTail, node)) return true;
            } else {
                tail.compareAndSet(tempTail, next);
            }
        }
    }

    public Integer deq() {
        // implement your deq method here
        while(true) {
            Node tempHead = head.get();
            Node tempTail = tail.get();
            Node next = tempHead.next.get();
            if(tempHead == head.get()) {
                if(tempHead == tempTail) {
                    if(next == null) {
                        return null;
                    }
                    tail.compareAndSet(tempTail, next);
                }
            } else {
                if(head.compareAndSet(tempHead, next)) return next.value;
            }

        }
    }

    protected class Node {
        public Integer value;
        public AtomicReference<Node> next;

        public Node(Integer x) {
            value = x;
            next = null;
        }
    }
}
