package q6.queue;

import java.util.List;
import java.util.*;
import java.util.concurrent.*;

import org.junit.Assert;
import org.junit.Test;

public class QueueTest {

    @Test
    public void testLockQueue() throws InterruptedException, ExecutionException {
        final int NUM_THREADS = 3;
        final LockQueue queue = new LockQueue();
        ExecutorService threadPool = Executors.newFixedThreadPool(NUM_THREADS);
        final CountDownLatch latch = new CountDownLatch(NUM_THREADS);
        List<Future<Integer>> futuresPut = new ArrayList<Future<Integer>>();
        for (int i = 0; i < 3; i++) {
            Future<Integer> submit = threadPool.submit(new Callable<Integer>() {
                public Integer call() {
                    int sum = 0;
                    for (int i = 0; i < 1000; i++) {
                        int nextInt = ThreadLocalRandom.current().nextInt(100);
                        queue.enq(nextInt);
                        sum += nextInt;
                    }
                    latch.countDown();
                    return sum;
                }
            });
            futuresPut.add(submit);
        }
        List<Future<Integer>> futuresGet = new ArrayList<Future<Integer>>();
        for (int i = 0; i < 3; i++) {
            Future<Integer> submit = threadPool.submit(new Callable<Integer>() {
                public Integer call() {
                    int count = 0;
                    try {
                        for (int i = 0; i < 1000; i++) {
                            Integer got = queue.deq();
                            count += got;
                        }
                    } catch (Exception e) {
                        System.out.println(e);
                    }
                    latch.countDown();
                    return count;
                }
            });
            futuresGet.add(submit);
        }
        latch.await();
        int sumPut = 0;
        for (Future<Integer> future : futuresPut) {
            sumPut += future.get();
        }
        int sumGet = 0;
        for (Future<Integer> future : futuresGet) {
            sumGet += future.get();
        }
        Assert.assertEquals(sumPut, sumGet);
    }

    @Test
    public void testLockFreeQueue() throws InterruptedException, ExecutionException {
        final int NUM_THREADS = 3;
        final LockFreeQueue queue = new LockFreeQueue();
        ExecutorService threadPool = Executors.newFixedThreadPool(NUM_THREADS);
        final CountDownLatch latch = new CountDownLatch(NUM_THREADS);
        List<Future<Integer>> futuresPut = new ArrayList<Future<Integer>>();
        for (int i = 0; i < 3; i++) {
            Future<Integer> submit = threadPool.submit(new Callable<Integer>() {
                public Integer call() {
                    int sum = 0;
                    for (int i = 0; i < 1000; i++) {
                        int nextInt = ThreadLocalRandom.current().nextInt(100);
                        queue.enq(nextInt);
                        sum += nextInt;
                    }
                    latch.countDown();
                    return sum;
                }
            });
            futuresPut.add(submit);
        }
        List<Future<Integer>> futuresGet = new ArrayList<Future<Integer>>();
        for (int i = 0; i < 3; i++) {
            Future<Integer> submit = threadPool.submit(new Callable<Integer>() {
                public Integer call() {
                    int count = 0;
                    try {
                        for (int i = 0; i < 1000; i++) {
                            Integer got = queue.deq();
                            count += got;
                        }
                    } catch (Exception e) {
                        System.out.println(e);
                    }
                    latch.countDown();
                    return count;
                }
            });
            futuresGet.add(submit);
        }
        latch.await();
        int sumPut = 0;
        for (Future<Integer> future : futuresPut) {
            sumPut += future.get();
        }
        int sumGet = 0;
        for (Future<Integer> future : futuresGet) {
            sumGet += future.get();
        }
        Assert.assertEquals(sumPut, sumGet);
    }
}