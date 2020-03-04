package q6.queue;

import java.util.concurrent.atomic.AtomicReference;

public class LockFreeQueue implements MyQueue {
    // you are free to add members
    AtomicReference<Node> head;
    AtomicReference<Node> tail;

    public LockFreeQueue() {
        // implement your constructor here
        Node node = new Node(null);
        head = new AtomicReference<Node>(node);
        tail = new AtomicReference<Node>(node);
    }

    public boolean enq(Integer value) {
        // implement your enq method here
        Node node = new Node(value);
        while(true) {
            Node tempTail = tail.get();
            if(tempTail == tail.get()){
                if(tempTail.next.get() == null) {
                    if(tempTail.next.compareAndSet(null, node)) {
                        tail.compareAndSet(tempTail, node);
                        return true;
                    } 
                } else {
                    Node tailNext = tempTail.next.get();
                    tail.compareAndSet(tempTail, tailNext);
                }
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
                else {
                    if(head.compareAndSet(tempHead, next)) return next.value;
                }
            }

        }
    }

    protected class Node {
        public Integer value;
        public AtomicReference<Node> next;

        public Node(Integer x) {
            value = x;
            next = new AtomicReference<Node>(null);
        }
    }
}
