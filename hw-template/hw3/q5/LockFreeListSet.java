package q5;

import java.util.concurrent.atomic.AtomicMarkableReference;

public class LockFreeListSet implements ListSet {
    // you are free to add members
    Node head;

    public LockFreeListSet() {
        // implement your constructor here
        head = new Node(0);
    }

    public boolean add(int value) {
        // implement your add method here
        Node n = new Node(value);
        Node prev = head;
        Node curr = head.next.getReference();

        while(curr != null) {
            if(curr.value == value) {
                return false;
            }
            if(curr.value > value) {
                break;
            }
            prev = curr;
            curr = curr.next.getReference();
        }

        n.next.set(curr, false);

        boolean success = prev.next.compareAndSet(curr, n, false, false);
        if(!success) {
            System.out.println(value);
        }
        return true;
    }

    public boolean remove(int value) {
        // implement your remove method here
        Node prev = head;
        Node curr = head.next.getReference();
        while(curr != null) {
            if(curr.value == value) break;
            if(curr.value > value) return false;
            prev = curr;
            curr = curr.next.getReference();
        }
        curr.next.compareAndSet(curr.next.getReference(), null, false, true);
        boolean success = prev.next.compareAndSet(curr, curr.next.getReference(), false, false);
        return success;
    }

    public boolean contains(int value) {
        // implement your contains method here
        Node curr = head.next.getReference();
        while(curr != null) {
            if(curr.value == value && !curr.next.isMarked()){
                return true;
            }
            curr = curr.next.getReference();
        }
        return false;
    }

    protected class Node {
        public Integer value;
        // public Node next;
        public AtomicMarkableReference<Node> next;
        public Node(Integer x) {
            value = x;
            next = new AtomicMarkableReference<Node>(null, false);
        }
    }

    /*
      return the string of list, if: 1 -> 2 -> 3, then return "1,2,3,"
      check simpleTest for more info
    */
    public String toString() {
        StringBuilder sb = new StringBuilder();
        Node curr = head.next.getReference();

        while(curr != null) {
            sb.append(curr.value).append(",");
            curr = curr.next.getReference();
        }
        
        return sb.toString();
    }
}
