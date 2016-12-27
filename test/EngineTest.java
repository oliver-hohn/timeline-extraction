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
     *
     * @throws ParseException thrown when we create the Dates for the expected TimelineDates and Results.
     */
    @Test
    public void testDates() throws ParseException {
        String sampleText = "On the 12th of December I ran tests on my final year project. The tests did not go so well. " +
                "Yesterday I played games. " + "It was fun playing games! Tomorrow I am going to study. Last week I went to watch a football match.";
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

        Engine engine = new Engine();
        ArrayList<Result> results = engine.getResults(sampleText, "2016-12-23");
        compareExpectedToActualDate(results, expectedResults);
    }

    /**
     * Tests the Engines ability to pick out Subjects from a given Sample text, and checks it to the expected Subjects
     * for that text.
     */
    @Test
    public void testSubjects() {
        //picks up last 16 as a date (issue with casual language used)
        String sampleText = "Manchester City face Monaco in the Champions League, with the first leg on 21 February. In their last season, Liverpool narrowly missed out on the 2013/14 Premier League title to Manchester City, while Leicester won last year's title only playing domestically. ";
        ArrayList<Result> expectedResults = new ArrayList<>();//just looking at subjects, not dates
        Result result1 = new Result();//two sentences, both with dates, so two results made
        Result result2 = new Result();//should pick out the team names: Monaco, Liverpool, Manchester City, Leicester, and Competitions: Champions League, Premier League
        //for the first sentence, the teams mentioned
        result1.addSubject("Champions League");
        result1.addSubject("Monaco");
        result1.addSubject("Manchester City");

        result2.addSubject("Liverpool");
        result2.addSubject("Manchester City");
        result2.addSubject("Premier League");
        result2.addSubject("Leicester");

        expectedResults.add(result1);
        expectedResults.add(result2);

        Engine engine = new Engine();
        ArrayList<Result> actualResults = engine.getResults(sampleText, "26-12-2016");

        compareExpectedToActualSubject(actualResults, expectedResults);
    }


    /**
     * Compares an actual list of Results with a expected one, by just looking at the dates picked out. Checking that the Engine
     * is picking out the right sentence and producing the right dates, not caring about the sentence trimming or subject picking.
     *
     * @param actualResults   the list of produced Result objects
     * @param expectedResults the list of expected Result objects
     */
    private void compareExpectedToActualDate(ArrayList<Result> actualResults, ArrayList<Result> expectedResults) {
        System.out.println("Actual Results: " + actualResults);
        Assert.assertEquals(expectedResults.size(), actualResults.size());//if this does not hold then the test fails
        for (int i = 0; i < actualResults.size(); i++) {
            Assert.assertEquals(expectedResults.get(i).getTimelineDate(), actualResults.get(i).getTimelineDate());
        }
    }

    /**
     * Compares the Subjects (only) in the list of Expected Result objects to the list of Actual Result objects.
     * Compares each actual Result object individually to its expected counterpart.
     *
     * @param actualResults   the list of produced Result objects
     * @param expectedResults the list of expected Result objects
     */
    private void compareExpectedToActualSubject(ArrayList<Result> actualResults, ArrayList<Result> expectedResults) {
        System.out.println("Actual Results: " + actualResults);
        System.out.println("Expected Results: " + expectedResults);
        Assert.assertEquals(expectedResults.size(), actualResults.size());
        for (int i = 0; i < actualResults.size(); i++) {
            Assert.assertEquals(expectedResults.get(i).getSubjects(), actualResults.get(i).getSubjects());
        }
    }
}
