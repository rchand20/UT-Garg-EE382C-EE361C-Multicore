package q3;

import java.util.concurrent.Semaphore;

public class Monkey {

    static int currDirection;
    static int count;
    static Semaphore rope;

    public Monkey() {
        currDirection = 0;
        count = 0;
        rope = new Semaphore(3, true);
    }

    public void ClimbRope(int direction) throws InterruptedException {
        
        // System.out.println(currDirection);
        // System.out.println("direction requested: " + direction);
        // System.out.println("available permits: " + rope.availablePermits());
        if(direction == -1) {
            rope.acquire(3);
            currDirection = -1;
        }

        else if (direction == currDirection || rope.availablePermits() == 3) {
            //System.out.println("acquiring the rope");
            currDirection = direction;
            
            rope.acquire();
        }

        else {
            while (rope.availablePermits() != 3) {
            }
            currDirection = direction;
            rope.acquire();
        }
    }

    public void LeaveRope() {
        if(currDirection == -1) {
            rope.release(3);
        } 

        else {
            rope.release();
        }
    }

    /**
     * Returns the number of monkeys on the rope currently for test purpose.
     *
     * @return the number of monkeys on the rope
     *
     *         Positive Test Cases: case 1: when normal monkey (0 and 1) is on the
     *         rope, this value should <= 3, >= 0 case 2: when Kong is on the rope,
     *         this value should be 1
     */
    public int getNumMonkeysOnRope() {
        return 3 - rope.availablePermits();
    }

}
