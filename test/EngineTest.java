import org.junit.Assert;
import org.junit.Test;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;

/**
 * Test class for the Engine. Gives in a sample text, and compares that the generated and predicted Result objects are equal
 */
public class EngineTest {
    private final static SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd");
    /**
     * Gives Engine sample text and compares the expected and generated output.
     * Only compares the output by the date (checking that it is picking out the right sentence, not looking at sentence trimming).
     */
    @Test
    public void testOutput() throws ParseException {
        String sampleText = "On the 12th of December I ran tests on my final year project. The tests did not go so well. "+
                "Yesterday I played games. "+"It was fun playing games! Tomorrow I am going to study. Last week I went to watch a football match.";
        ArrayList<Result> expectedResults = new ArrayList<>();

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

/*        for (Result result: expectedResults){
            System.out.println(result);
        }*/

        Engine engine = new Engine();
        ArrayList<Result> results = engine.getResults(sampleText, "2016-12-23");

/*        for(Result result: results){
            System.out.println(result);
        }*/
        Assert.assertEquals(results.size(), expectedResults.size());//if this does not hold then the test fails
        for(int i=0; i<results.size(); i++){
            Assert.assertEquals(results.get(i).getTimelineDate(), expectedResults.get(i).getTimelineDate());
        }
    }
}
