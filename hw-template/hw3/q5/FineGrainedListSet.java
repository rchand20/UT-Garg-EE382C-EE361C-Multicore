package q5;

import java.util.concurrent.locks.ReentrantLock;

public class FineGrainedListSet implements ListSet {
    // you are free to add members

    Node head;

    public FineGrainedListSet() {
        // implement your constructor here
        head = new Node(null);
    }

    public boolean add(int value) {
        // implement your add method here
        head.lock.lock();
        Node newNode = new Node(value);

        Node prev;
        Node curr;

        prev = head;
        curr = head.next;

        if(curr != null) {
            curr.lock.lock();
        }

        while(curr != null) {
            if(curr.value == value) {
                curr.lock.unlock();
                prev.lock.unlock();
                return false;
            }
            if(curr.value > value) {
                break;
            }  

            prev.lock.unlock();
 
            prev = curr;
            curr = prev.next;

            if(curr != null) {
                curr.lock.lock();
            }

        }

        newNode.next = curr;
        prev.next = newNode;

        prev.lock.unlock();
        if(curr != null) {
            curr.lock.unlock();
        }

        return true;

    }

    public boolean remove(int value) {
        // implement your remove method here
        head.lock.lock();
        Node prev = head;
        Node curr = head.next;

        if(curr != null) {
            curr.lock.lock();
        }

        while(curr != null) {
            if(curr.value == value) {
                prev.next = curr.next;
                curr.lock.unlock();
                prev.lock.unlock();

                return true;
            }

            prev.lock.unlock();
 
            prev = curr;
            curr = prev.next;

            if(curr != null) {
                curr.lock.lock();
            }
        }
        return false;
    }

    public boolean contains(int value) {
        // implement your contains method here
        head.lock.lock();
        Node prev = head;
        Node curr = head.next;

        if(curr != null) {
            curr.lock.lock();
        } else {
            return false;
        }

        while(curr != null) {
            if(curr.value == value) {
                curr.lock.unlock();
                prev.lock.unlock();
                return true;
            }

            prev.lock.unlock();
 
            prev = curr;
            curr = prev.next;

            if(curr != null) {
                curr.lock.lock();
            }
        }

        return false;
    }

    protected class Node {
        public Integer value;
        public Node next;
        public ReentrantLock lock;

        public Node(Integer x) {
            value = x;
            next = null;
            lock = new ReentrantLock();
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
