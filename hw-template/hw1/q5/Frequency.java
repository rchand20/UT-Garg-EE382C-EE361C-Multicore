package q5;
import java.util.concurrent.Callable;
import java.util.concurrent.FutureTask;

import java.util.concurrent.Callable;

public class Frequency implements Callable<Integer> {
    int start;
    int end;
    int x;
    int count;
    int[] array;

    public Frequency(int start, int end, int x, int[] array) {
        this.start = start;
        this.end = end;
        this.x = x;
        this.array = array;
        this.count = 0;
    }

    public static int parallelFreq(int x, int[] A, int numThreads){
        //your implementation goes here, return -1 if the input is not valid.
        int start = 0;
        int end = A.length / numThreads;
        int count = 0;
        FutureTask[] threads = new FutureTask[numThreads];

        for(int i = 0; i < numThreads; i++) {

            Callable<Integer> callable = new Frequency(start, end, x, A);
            start = end;
            end += A.length/numThreads;

            if(i == numThreads - 2) {
                end = A.length;
            }

            threads[i] = new FutureTask(callable);
            Thread t = new Thread(threads[i]);
            t.start();
        }
        
        for(int i = 0; i < numThreads; i++) {
            try {
                count += (int) threads[i].get();
            } catch (Exception e) {
                System.out.print(e);
            }
        }

        return count;
    }

    @Override
    public Integer call() throws Exception {
        for(int i = start; i < end; i++) {
            if(array[i] == x) count++;
        }
        return count;
    }
}
