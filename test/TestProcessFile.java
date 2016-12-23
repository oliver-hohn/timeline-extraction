import org.junit.Assert;
import org.junit.Test;

import java.io.File;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;

/**
 * Created by Oliver on 23/12/2016.
 */
//TODO: documentation
public class TestProcessFile {
    private SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd");
    @Test
    public void testOutput() throws ParseException, InterruptedException {
        ArrayList<Result> expectedResults = new ArrayList<>();
        final ArrayList<Result>[] actualResults = new ArrayList[1];

        Result result1 = new Result();
        TimelineDate timelineDate1 = new TimelineDate();
        timelineDate1.setDate1(simpleDateFormat.parse("2016-12-12"));
        result1.setTimelineDate(timelineDate1);
        expectedResults.add(result1);

        Result result2 = new Result();
        TimelineDate timelineDate2 = new TimelineDate();
        timelineDate2.setDate1(simpleDateFormat.parse("2016-12-22"));
        result2.setTimelineDate(timelineDate2);
        expectedResults.add(result2);

        Result result3 = new Result();
        TimelineDate timelineDate3 = new TimelineDate();
        timelineDate3.setDate1(simpleDateFormat.parse("2016-12-24"));
        result3.setTimelineDate(timelineDate3);
        expectedResults.add(result3);

        Result result4 = new Result();
        TimelineDate timelineDate4 = new TimelineDate();
        timelineDate4.setDate1(simpleDateFormat.parse("2016-12-12"));
        timelineDate4.setDate2(simpleDateFormat.parse("2016-12-18"));
        result4.setTimelineDate(timelineDate4);
        expectedResults.add(result4);

        File testFile = new File("D:"+File.separator+"FYP"+File.separator+"testfile.txt");
        ArrayList<File> files = new ArrayList<>();
        files.add(testFile);

        ProcessFiles processFiles = new ProcessFiles();
        processFiles.processFiles(files, new CallbackResults() {
            @Override
            public void gotResults(ArrayList<Result> results) {
                System.out.println("Got Results");
                actualResults[0] = results;

            }
        });
        Thread.sleep(5000);
        Assert.assertEquals(actualResults[0].size(), expectedResults.size());//if this does not hold then the test fails
        for(int i=0; i<actualResults[0].size(); i++){
            Assert.assertEquals(actualResults[0].get(i).getTimelineDate(), expectedResults.get(i).getTimelineDate());
        }
    }
}
