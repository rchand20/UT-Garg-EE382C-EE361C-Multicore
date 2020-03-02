package q5;

import java.util.concurrent.locks.ReentrantLock;

public class CoarseGrainedListSet implements ListSet {
    // you are free to add members
    ReentrantLock lock;
    Node head;


    public CoarseGrainedListSet() {
        // implement your constructor here
        lock = new ReentrantLock();
        head = new Node(0);
    }

    public boolean add(int value) {
        // implement your add method here
        Node n = new Node(value);
        lock.lock();

        Node prev = head;
        Node curr = head.next;

        while(curr != null) {

            if(curr.value == value) {
                lock.unlock();
                return false;
            }
            if(curr.value > value) {
                break;
            }  

            prev = curr;
            curr = curr.next;
        }

        n.next = curr;
        prev.next = n;

        lock.unlock();

        return true;
    }

    public boolean remove(int value) {
        // implement your remove method here
        return false;
    }

    public boolean contains(int value) {
        // implement your contains method here
        return false;
    }

    protected class Node {
        public Integer value;
        public Node next;

        public Node(Integer x) {
            value = x;
            next = null;
        }
    }

    /*
      return the string of list, if: 1 -> 2 -> 3, then return "1,2,3,"
      check simpleTest for more info
    */
    public String toString() {
        StringBuilder sb = new StringBuilder();
        Node curr = head.next;

        while(curr != null) {
            sb.append(curr.value).append(",");
            curr = curr.next;
        }
        
        return sb.toString();
    }
}
