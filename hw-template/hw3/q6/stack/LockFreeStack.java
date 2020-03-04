package q6.stack;

import java.util.concurrent.atomic.AtomicReference;

public class LockFreeStack implements MyStack {
    // you are free to add members
    AtomicReference<Node> top;

    public LockFreeStack() {
        // implement your constructor here
        top = new AtomicReference<Node>();

    }

    public boolean push(Integer value) {
        // implement your push method here
        Node node = new Node(value);
        while(true) {
            Node oldTop = top.get();
            node.next = oldTop;
            if(top.compareAndSet(oldTop, node)) {
                return true;
            }
            else {
                Thread.yield();
            }

            return false;
        }

    }

    public Integer pop() throws EmptyStack {
        // implement your pop method here
        while(true) {
            Node old = top.get();
            if(old == null) {
                throw new EmptyStack();
            }

            Integer val = old.value;
            Node newTop = old.next;
            if(top.compareAndSet(old, newTop)) {
                return val;
            } else {
                Thread.yield();
            }
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
