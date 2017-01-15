package backend;

import backend.process.*;
import javafx.concurrent.Task;
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
        public void gotResults(ArrayList<Result> results, ArrayList<FileData> fileDataList) {
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
    public void testSampleFileProcessTXT() throws ParseException, InterruptedException {
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
        actualResults = processFiles.processFiles(files).first();
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
        Task<Boolean> task = new Task<Boolean>() {
            @Override
            protected Boolean call() throws Exception {
                processFiles.processFiles(files).first();
                return null;
            }
        };
        new Thread(task).start();
        System.out.println("Actual Threads running" + Thread.activeCount());
        Assert.assertEquals(true, Thread.activeCount() >= 2);
        Thread.sleep(10000);//give the backend.process.Engine time to finish running
        Assert.assertEquals(true,true);//seems to be an error that if you finish with a thread sleep it will stop running the other operations on other threads (i.e. releasing semaphores and setting state to FINISHED).
    }

    /**
     * Tests the processing of a sample docx file, by checking the processed Result against expected Results. Only looks
     * at dates picked out (checking only it is processing the File correctly, i.e. picking out the correct sentences, and
     * their dates).
     *
     * @throws InterruptedException as we are putting the Thread that runs this test to sleep to let ProcessFile finish running.
     * @throws ParseException       for the Dates that we create for the expected Results.
     */
    @Test
    public void testSampleFileProcessDocx() throws InterruptedException, ParseException {
        actualResults = null;
        ArrayList<Result> expectedResults = new ArrayList<>();
        //base date is 29/12/2016 (creation date of file)
        Result result1 = new Result();
        TimelineDate timelineDate1 = new TimelineDate();
        timelineDate1.setDate1(simpleDateFormat.parse("2016-12-30"));
        result1.setTimelineDate(timelineDate1);
        expectedResults.add(result1);

        Result result2 = new Result();
        TimelineDate timelineDate2 = new TimelineDate();
        timelineDate2.setDate1(simpleDateFormat.parse("2008-01-01"));//second sentence talks about donations between 2008 and spring of this year (2016)
        timelineDate2.setDate2(simpleDateFormat.parse("2016-05-31"));//last day of spring
        result2.setTimelineDate(timelineDate2);
        expectedResults.add(result2);

        Result result3 = new Result();
        TimelineDate timelineDate3 = new TimelineDate();
        timelineDate3.setDate1(simpleDateFormat.parse("1980-01-01"));//donated since the early 1980s (range so 1980 ->1989)
        timelineDate3.setDate2(simpleDateFormat.parse("1989-12-31"));//last day of the 1980s
        result3.setTimelineDate(timelineDate3);
        expectedResults.add(result3);

        Result result4 = new Result();
        TimelineDate timelineDate4 = new TimelineDate();
        timelineDate4.setDate1(simpleDateFormat.parse("1996-01-01"));//in 1996 an event happened (doesnt specify day or month, just year)
        result4.setTimelineDate(timelineDate4);
        expectedResults.add(result4);

        File testFile = new File("test/resources/testfile4.docx");
        ArrayList<File> files = new ArrayList<>();
        files.add(testFile);

        ProcessFiles processFiles = new ProcessFiles();
        actualResults = processFiles.processFiles(files).first();
        compareExpectedToActual(actualResults, expectedResults);
    }

    /**
     * Test for processing PDF Files, by checking the processed Results to the expected Results. Checks only their dates
     * as we are interested in it picking out the right sentences and their dates in the documents parsed in.
     *
     * @throws InterruptedException as we are putting the Thread that runs this test to sleep to allow time for ProcessFile to finish (runs on separate Thread).
     * @throws ParseException       for the Dates that we create for the expected Results.
     */
    @Test
    public void testSampleFilePDF() throws InterruptedException, ParseException {
        actualResults = null;
        ArrayList<Result> expectedResults = new ArrayList<>();
        //base date 2016-12-29

        //on Friday = 2016-12-30
        //1967 = 1967-01-01
        Result result1 = new Result();
        TimelineDate timelineDate1 = new TimelineDate();
        timelineDate1.setDate1(simpleDateFormat.parse("2016-12-30"));
        result1.setTimelineDate(timelineDate1);
        expectedResults.add(result1);

        Result result2 = new Result();
        TimelineDate timelineDate2 = new TimelineDate();
        timelineDate2.setDate1(simpleDateFormat.parse("1967-01-01"));
        result2.setTimelineDate(timelineDate2);
        expectedResults.add(result2);

        File testFile = new File("test/resources/testfile5.pdf");//bbc article (http://www.bbc.com/news/world-us-canada-38451258)
        ArrayList<File> files = new ArrayList<>();
        files.add(testFile);

        ProcessFiles processFiles = new ProcessFiles();
        actualResults = processFiles.processFiles(files).first();
        System.out.println(actualResults);
        compareExpectedToActual(actualResults, expectedResults);
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
