package q6.Bakery;

import java.util.Arrays;
import java.util.concurrent.atomic.AtomicInteger; 
import java.util.concurrent.atomic.AtomicBoolean; 

public class BakeryLock implements Lock {

    int numThreads;
    AtomicBoolean[] choosing;
    AtomicInteger[] number;


    public BakeryLock(int numThreads){
        // your implementation goes here.
        this.numThreads = numThreads;
        choosing = new AtomicBoolean[numThreads];
        number = new AtomicInteger[numThreads];
        Arrays.fill(choosing, new AtomicBoolean());
        Arrays.fill(number, new AtomicInteger());
    }

    @Override
    public void lock(int pid) {
        // TODO Auto-generated method stub
        choosing[pid].set(true);
        for(int i = 0; i < numThreads; i++) {
            if(number[i].get() > number[pid].get()) {
                number[pid] = number[i];
            }
        }

        number[pid].getAndIncrement();
        choosing[pid].set(false);

        for(int i = 0; i < numThreads; i++) {
            while(choosing[i].get());
            while((number[i].get() != 0) && ((number[i].get() < number[pid].get()) || ((number[i].get() == number[pid].get()) && i < pid)));
        }
    }

    @Override
    public void unlock(int pid) {
        // TODO Auto-generated method stub
        number[pid].set(0);

    }
}
