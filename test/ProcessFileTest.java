import backend.process.CallbackResults;
import backend.process.ProcessFiles;
import backend.process.Result;
import backend.process.TimelineDate;
import org.junit.Assert;
import org.junit.Test;

import java.io.File;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;

/**
 * Tests the the extracting of text in files and passing it to the backend.process.Engine, and the fact that Threads are being created
 * to process the Files separately.
 */

public class ProcessFileTest {
    private SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd");
    private ArrayList<Result> actualResults;
    private CallbackResults callbackResults = new CallbackResults() {
        @Override
        public void gotResults(ArrayList<Result> results) {
            System.out.println("Got results");
            actualResults = results;
        }
    };

    /**
     * Chaining tests (all in same class running one after the other) for ProcessFile will overlap, as when the next
     * test runs, the other has not finished.
     * <p>
     * Test for processing a test file with sample text, and comparing its output to expected backend.process.Result objects.
     *
     * @throws ParseException       for the Dates that we create for the expected Results.
     * @throws InterruptedException as we are putting the Thread that runs this test to sleep to let ProcessFile finish running.
     */
    @Test
    public void testSampleFileProcess() throws ParseException, InterruptedException {
        ArrayList<Result> expectedResults = new ArrayList<>();
        actualResults = null;

        Result result1 = new Result();
        TimelineDate timelineDate1 = new TimelineDate();
        timelineDate1.setDate1(simpleDateFormat.parse("2016-12-12"));
        result1.setTimelineDate(timelineDate1);
        expectedResults.add(result1);

        Result result2 = new Result();
        TimelineDate timelineDate2 = new TimelineDate();
        timelineDate2.setDate1(simpleDateFormat.parse("2016-12-27"));
        result2.setTimelineDate(timelineDate2);
        expectedResults.add(result2);

        Result result3 = new Result();
        TimelineDate timelineDate3 = new TimelineDate();
        timelineDate3.setDate1(simpleDateFormat.parse("2016-12-29"));
        result3.setTimelineDate(timelineDate3);
        expectedResults.add(result3);

        Result result4 = new Result();
        TimelineDate timelineDate4 = new TimelineDate();
        timelineDate4.setDate1(simpleDateFormat.parse("2016-12-19"));
        timelineDate4.setDate2(simpleDateFormat.parse("2016-12-25"));
        result4.setTimelineDate(timelineDate4);
        expectedResults.add(result4);

        File testFile = new File("test/resources/testfile1.txt");
        ArrayList<File> files = new ArrayList<>();
        files.add(testFile);

        ProcessFiles processFiles = new ProcessFiles();
        processFiles.processFiles(files, callbackResults);
        Thread.sleep(10000);
        compareExpectedToActual(actualResults, expectedResults);
    }

    /**
     * Tests that more than one Thread is running after calling ProcessFile (as a minimum it will create one extra Thread
     * to process the passed in files).
     *
     * @throws InterruptedException as we put the Thread that runs this test to sleep, so that ProcessFile can finish running.
     */
    @Test
    public void testMultiThread() throws InterruptedException {//check that processfile makes two more threads run
        // at least 2 Threads should be running (main and the one created by process file).
        actualResults = null;
        File testFile1 = new File("test/resources/testfile1.txt");
        File testFile2 = new File("test/resources/testfile2.txt");
        File testFile3 = new File("test/resources/testfile3.txt");

        ArrayList<File> files = new ArrayList<>();
        files.add(testFile1);
        files.add(testFile2);
        files.add(testFile3);

        ProcessFiles processFiles = new ProcessFiles();
        processFiles.processFiles(files, callbackResults);
        System.out.println("Actual Threads running" + Thread.activeCount());
        Assert.assertEquals(true, Thread.activeCount() >= 2);
        Thread.sleep(5000);//give the backend.process.Engine time to finish running
    }

    /**
     * Compares an actual list of Results with a expected one, by just looking at the dates picked out. Checking that the backend.process.Engine
     * is picking out the right sentence and producing the right dates, not caring about the sentence trimming or subject picking.
     *
     * @param actualResults   the list of produced backend.process.Result objects
     * @param expectedResults the list of expected backend.process.Result objects
     */
    private void compareExpectedToActual(ArrayList<Result> actualResults, ArrayList<Result> expectedResults) {
        System.out.println("Actual Results: " + actualResults);
        Assert.assertEquals(actualResults.size(), expectedResults.size());//if this does not hold then the test fails
        for (int i = 0; i < actualResults.size(); i++) {
            Assert.assertEquals(actualResults.get(i).getTimelineDate(), expectedResults.get(i).getTimelineDate());
        }
    }
}
