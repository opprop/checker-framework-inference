package checkers.system;

import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import org.junit.Test;

import checkers.inference.solver.util.Statistics;
import junit.framework.TestCase;

public class StatisticsMultithreadTest extends TestCase {

    public static final int Max_Threads = 100;
    public static final int Threads = 100;

    private ExecutorService executor;

    @Override
    protected void setUp() throws Exception {
        executor = Executors.newFixedThreadPool(Max_Threads);
    }

    @Override
    protected void tearDown() throws Exception {
        System.out.println("Statistics collected:");
        for (Entry<String, Long> entry : Statistics.getStatistics().entrySet()) {
            System.out.println(entry.getKey() + " --> " + entry.getValue());
        }
        Statistics.clearStatistics();
        executor = null;
    }

    /**
     * lambda interface for creating threads
     * 
     * each thread is given a unique threadID and the return object must implement {@link Runnable}
     * 
     * @param <Runnable>
     */
    @SuppressWarnings("hiding")
    private interface ThreadMaker<Runnable> {
        Runnable make(int threadID);
    }

    /**
     * Helper which runs a number of threads and waits until all threads have completed.
     * 
     * @param threadMaker
     *            lambda parameter for fresh Runnable objects
     */
    private void runThreads(ThreadMaker<Runnable> threadMaker) {
        // create and execute 100 threads, each trying to add or update an entry to the statistics
        for (int threadID = 0; threadID < Threads; threadID++) {
            executor.execute(threadMaker.make(threadID));
        }
        // initiate clean shutdown of executor
        executor.shutdown();
        // wait for all threads to finish, up to 1 min
        try {
            executor.awaitTermination(1, TimeUnit.MINUTES);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        // System.out.println("Finished all threads.");
    }

    // =======================================

    // must be lower case for retrieval
    public static final String IncrementEntryKey = "incremententrykey";
    public static final long IncrementEntryVal = 100L;

    @Test
    public void testAddOrIncrementEntry() {
        runThreads(threadID -> new AddOrIncrementEntryTestThread());

        // check that the entry in the statistics match the expected value
        Map<String, Long> finalStatistics = Statistics.getStatistics();
        assertEquals(finalStatistics.get(IncrementEntryKey).longValue(),
                IncrementEntryVal * Threads);
    }

    private class AddOrIncrementEntryTestThread implements Runnable {
        @Override
        public void run() {
            Statistics.addOrIncrementEntry(IncrementEntryKey, IncrementEntryVal);
        }
    }

    // =======================================

    // recordSlotsStatistics

    // recordConstraintsStatistics

}
