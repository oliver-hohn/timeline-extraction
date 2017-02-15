package backend;

import backend.process.Result;
import backend.process.TimelineDate;
import backend.ranges.ProduceRanges;
import backend.ranges.Range;
import org.junit.Assert;
import org.junit.Test;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;

/**
 * Test class for the processing of Result Lists into Range Trees.
 */
public class ProduceRangesTest {

    /**
     * Tests a simple Range Tree formed by processing the fake List of Results, by matching it to an expected Range Tree.
     *
     * @throws ParseException when setting the Dates for the fake Results.
     */
    @Test
    public void testSingleTree() throws ParseException {
        List<Result> resultList = new ArrayList<>();

        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("dd-MM-yyyy G");

        List<Range> expectedTrees = new ArrayList<>();
        Range range = new Range(simpleDateFormat.parse("01-01-0001 AD"), simpleDateFormat.parse("31-12-9999 AD"));
        Range range1980 = new Range(simpleDateFormat.parse("01-01-1980 AD"), simpleDateFormat.parse("31-12-1989 AD"));
        Range range2008 = new Range(simpleDateFormat.parse("01-01-2008 AD"), simpleDateFormat.parse("31-05-2016 AD"));
        Range range2015 = new Range(simpleDateFormat.parse("01-01-2015 AD"), null);
        range2008.addChild(range2015);
        range.addChild(range2008);
        range.addChild(range1980);
        expectedTrees.add(range);

        Result result1;
        TimelineDate timelineDate1;
        result1 = new Result();
        timelineDate1 = new TimelineDate();
        timelineDate1.setDate1(simpleDateFormat.parse("01-01-0001 AD"));
        timelineDate1.setDate2(simpleDateFormat.parse("31-12-9999 AD"));
        result1.setTimelineDate(timelineDate1);
        resultList.add(result1);
        range.add(result1);
        result1 = new Result();
        timelineDate1 = new TimelineDate();
        timelineDate1.setDate1(simpleDateFormat.parse("01-01-1980 AD"));
        timelineDate1.setDate2(simpleDateFormat.parse("31-12-1989 AD"));
        result1.setTimelineDate(timelineDate1);
        resultList.add(result1);
        range1980.add(result1);
        result1 = new Result();
        timelineDate1 = new TimelineDate();
        timelineDate1.setDate1(simpleDateFormat.parse("01-01-2008 AD"));
        timelineDate1.setDate2(simpleDateFormat.parse("31-05-2016 AD"));
        result1.setTimelineDate(timelineDate1);
        resultList.add(result1);
        range2008.add(result1);
        result1 = new Result();
        timelineDate1 = new TimelineDate();
        timelineDate1.setDate1(simpleDateFormat.parse("01-01-2015 AD"));
        result1.setTimelineDate(timelineDate1);
        resultList.add(result1);
        range2015.add(result1);

        ProduceRanges produceRanges = new ProduceRanges();
        produceRanges.produceRanges(resultList);
        List<Range> actualTrees = produceRanges.getTrees();

        checkTrees(expectedTrees, actualTrees);

    }

    /**
     * Tests that multiple Trees are formed when the List of fake Results contains at least 2 disjoint Results (based on
     * their Dates), and then checks that the Trees formed match the expected List of Trees.
     *
     * @throws ParseException when setting the Dates for the fake Results.
     */
    @Test
    public void testSeparateTrees() throws ParseException {
        List<Result> resultList = new ArrayList<>();

        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("dd-MM-yyyy G");

        List<Range> expectedTrees = new ArrayList<>();
        Range range = new Range(simpleDateFormat.parse("01-01-0001 AD"), simpleDateFormat.parse("31-12-9999 AD"));
        Range range1980 = new Range(simpleDateFormat.parse("01-01-1980 AD"), simpleDateFormat.parse("31-12-1989 AD"));
        Range range2008 = new Range(simpleDateFormat.parse("01-01-2008 AD"), simpleDateFormat.parse("31-05-2016 AD"));
        Range range2015 = new Range(simpleDateFormat.parse("01-01-2015 AD"), null);
        range2008.addChild(range2015);
        range.addChild(range2008);
        range.addChild(range1980);
        expectedTrees.add(range);
        Range range400BC = new Range(simpleDateFormat.parse("01-01-499 BC"), simpleDateFormat.parse("31-12-400 BC"));
        expectedTrees.add(range400BC);

        Result result1;
        TimelineDate timelineDate1;
        result1 = new Result();
        timelineDate1 = new TimelineDate();
        timelineDate1.setDate1(simpleDateFormat.parse("01-01-0001 AD"));
        timelineDate1.setDate2(simpleDateFormat.parse("31-12-9999 AD"));
        result1.setTimelineDate(timelineDate1);
        resultList.add(result1);
        range.add(result1);
        result1 = new Result();
        timelineDate1 = new TimelineDate();
        timelineDate1.setDate1(simpleDateFormat.parse("01-01-1980 AD"));
        timelineDate1.setDate2(simpleDateFormat.parse("31-12-1989 AD"));
        result1.setTimelineDate(timelineDate1);
        resultList.add(result1);
        range1980.add(result1);
        result1 = new Result();
        timelineDate1 = new TimelineDate();
        timelineDate1.setDate1(simpleDateFormat.parse("01-01-2008 AD"));
        timelineDate1.setDate2(simpleDateFormat.parse("31-05-2016 AD"));
        result1.setTimelineDate(timelineDate1);
        resultList.add(result1);
        range2008.add(result1);
        result1 = new Result();
        timelineDate1 = new TimelineDate();
        timelineDate1.setDate1(simpleDateFormat.parse("01-01-2015 AD"));
        result1.setTimelineDate(timelineDate1);
        resultList.add(result1);
        range2015.add(result1);
        result1 = new Result();
        timelineDate1 = new TimelineDate();
        timelineDate1.setDate1(simpleDateFormat.parse("01-01-0499 BC"));
        timelineDate1.setDate2(simpleDateFormat.parse("31-12-0400 BC"));
        result1.setTimelineDate(timelineDate1);
        resultList.add(result1);
        range400BC.add(result1);


        ProduceRanges produceRanges = new ProduceRanges();
        produceRanges.produceRanges(resultList);
        List<Range> actualTrees = produceRanges.getTrees();

        checkTrees(expectedTrees, actualTrees);
    }

    /**
     * Tests the two Trees formed based on a list of fake Results. One of the Trees has a depth of 2, such that it contains
     * a Range with a sub-Range with a sub-sub-Range (more complex than previous tests). Checks that the Results produce
     * the expected two Range Trees (one in BC and one in AD), with the AD Range Tree containing 10 results.
     *
     * @throws ParseException when producing the Dates for the fake Results.
     */
    @Test
    public void testComplexSeparateTrees() throws ParseException {
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("dd-MM-yyyy G");
        List<Range> expectedTrees = new ArrayList<>();

        Range range01To99 = new Range(simpleDateFormat.parse("01-01-0001 AD"), simpleDateFormat.parse("31-12-9999 AD"));
        Range range1980 = new Range(simpleDateFormat.parse("01-01-1980 AD"), simpleDateFormat.parse("31-12-1989 AD"));
        Range range2008 = new Range(simpleDateFormat.parse("01-01-2008 AD"), simpleDateFormat.parse("31-05-2016 AD"));
        Range range2015 = new Range(simpleDateFormat.parse("01-01-2015 AD"), null);
        Range range201612To20 = new Range(simpleDateFormat.parse("12-12-2016 AD"), simpleDateFormat.parse("20-12-2016 AD"));
        Range range201612To18 = new Range(simpleDateFormat.parse("12-12-2016 AD"), simpleDateFormat.parse("18-12-2016 AD"));
        Range range201615To20 = new Range(simpleDateFormat.parse("15-12-2016 AD"), simpleDateFormat.parse("20-12-2016 AD"));
        Range range201601 = new Range(simpleDateFormat.parse("01-11-2016 AD"), null);
        Range range1996 = new Range(simpleDateFormat.parse("01-01-1996 AD"), null);
        Range range201623 = new Range(simpleDateFormat.parse("23-12-2016 AD"), null);
        Range range0499 = new Range(simpleDateFormat.parse("01-01-0499 BC"), simpleDateFormat.parse("31-12-0400 BC"));


        range01To99.getChildren().add(range201623);
        range01To99.getChildren().add(range201612To20);
        range01To99.getChildren().add(range201601);
        range01To99.getChildren().add(range2008);
        range01To99.getChildren().add(range1996);
        range01To99.getChildren().add(range1980);
        range2008.getChildren().add(range2015);
        range201612To20.getChildren().add(range201615To20);
        range201612To20.getChildren().add(range201612To18);

        expectedTrees.add(range01To99);
        expectedTrees.add(range0499);

        List<Result> results = new ArrayList<>();
        Result result;
        TimelineDate timelineDate;
        result = new Result();
        timelineDate = new TimelineDate();
        timelineDate.setDate1(simpleDateFormat.parse("01-01-0001 AD"));
        timelineDate.setDate2(simpleDateFormat.parse("31-12-9999 AD"));
        result.setTimelineDate(timelineDate);
        range01To99.add(result);
        range01To99.add(result);
        results.add(result);
        results.add(result);
        result = new Result();
        timelineDate = new TimelineDate();
        timelineDate.setDate1(simpleDateFormat.parse("01-01-1980 AD"));
        timelineDate.setDate2(simpleDateFormat.parse("31-12-1989 AD"));
        result.setTimelineDate(timelineDate);
        range1980.add(result);
        results.add(result);
        result = new Result();
        timelineDate = new TimelineDate();
        timelineDate.setDate1(simpleDateFormat.parse("01-01-2008 AD"));
        timelineDate.setDate2(simpleDateFormat.parse("31-05-2016 AD"));
        result.setTimelineDate(timelineDate);
        range2008.add(result);
        results.add(result);
        result = new Result();
        timelineDate = new TimelineDate();
        timelineDate.setDate1(simpleDateFormat.parse("01-01-2015 AD"));
        result.setTimelineDate(timelineDate);
        range2015.add(result);
        results.add(result);
        result = new Result();
        timelineDate = new TimelineDate();
        timelineDate.setDate1(simpleDateFormat.parse("12-12-2016 AD"));
        timelineDate.setDate2(simpleDateFormat.parse("18-12-2016 AD"));
        result.setTimelineDate(timelineDate);
        range201612To18.add(result);
        results.add(result);
        result = new Result();
        timelineDate = new TimelineDate();
        timelineDate.setDate1(simpleDateFormat.parse("15-12-2016 AD"));
        timelineDate.setDate2(simpleDateFormat.parse("20-12-2016 AD"));
        result.setTimelineDate(timelineDate);
        range201615To20.add(result);
        results.add(result);
        result = new Result();
        timelineDate = new TimelineDate();
        timelineDate.setDate1(simpleDateFormat.parse("01-11-2016 AD"));
        result.setTimelineDate(timelineDate);
        range201601.add(result);
        results.add(result);
        result = new Result();
        timelineDate = new TimelineDate();
        timelineDate.setDate1(simpleDateFormat.parse("01-01-1996 AD"));
        result.setTimelineDate(timelineDate);
        range1996.add(result);
        results.add(result);
        result = new Result();
        timelineDate = new TimelineDate();
        timelineDate.setDate1(simpleDateFormat.parse("23-12-2016 AD"));
        result.setTimelineDate(timelineDate);
        range201623.add(result);
        results.add(result);
        result = new Result();
        timelineDate = new TimelineDate();
        timelineDate.setDate1(simpleDateFormat.parse("01-01-0499 BC"));
        timelineDate.setDate2(simpleDateFormat.parse("31-12-0400 BC"));
        result.setTimelineDate(timelineDate);
        range0499.add(result);
        results.add(result);

        ProduceRanges produceRanges = new ProduceRanges();
        produceRanges.produceRanges(results);

        List<Range> actual = produceRanges.getTrees();
        checkTrees(expectedTrees, actual);
    }

    /**
     * Asserts that both lists of Ranges, the expected and actual, are equal, ie they are the same size, and that each
     * contain the same Range objects at the same indices.
     *
     * @param expected the expected list of Ranges.
     * @param actual   the actual list of Ranges.
     */
    private void checkTrees(List<Range> expected, List<Range> actual) {
        Assert.assertEquals(expected.size(), actual.size());
        for (int i = 0; i < expected.size(); i++) {
            Assert.assertEquals(expected.get(i), actual.get(i));
        }
    }
}
