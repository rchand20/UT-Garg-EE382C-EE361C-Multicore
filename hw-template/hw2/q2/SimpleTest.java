package q2;

import org.junit.Assert;
import org.junit.Test;

public class SimpleTest {

    private static final int OPERATIONS = 120000;
    private static final int NUM_THREADS = 8;

    @Test
    public void testCLH() {
        int result = q2.a.PIncrement.parallelIncrement(0, NUM_THREADS);
        //System.out.println("result is: " + result);
        Assert.assertEquals(OPERATIONS, result);
    }

    @Test
    public void testLamport() {
        int result = q2.b.PIncrement.parallelIncrement(0, NUM_THREADS);
       // System.out.println(result);
        Assert.assertEquals(result, OPERATIONS);
    }

    @Test
    public void testAnderson() {
        int result = q2.c.PIncrement.parallelIncrement(0, NUM_THREADS);
        //System.out.println(result);
        Assert.assertEquals(result, OPERATIONS);
    }

}
