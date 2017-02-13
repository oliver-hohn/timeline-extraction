package backend;

import backend.process.Result;
import backend.process.TimelineDate;
import backend.ranges.ProduceRanges;
import backend.ranges.Range;
import org.junit.Test;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by Oliver on 13/02/2017.
 */
public class ProduceRangesTest {
    @Test
    public void test() throws ParseException {
        List<Result> resultList = new ArrayList<>();
        Result result;
        TimelineDate timelineDate;
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("dd-MM-yyyy G");


        result = new Result();
        timelineDate = new TimelineDate();
        timelineDate.setDate1(simpleDateFormat.parse("01-01-0001 AD"));
        timelineDate.setDate2(simpleDateFormat.parse("31-12-9999 AD"));
        result.setTimelineDate(timelineDate);
        resultList.add(result);
        result = new Result();
        timelineDate = new TimelineDate();
        timelineDate.setDate1(simpleDateFormat.parse("12-12-2016 AD"));
        timelineDate.setDate2(simpleDateFormat.parse("18-12-2016 AD"));
        result.setTimelineDate(timelineDate);
        resultList.add(result);
        result = new Result();
        timelineDate = new TimelineDate();
        timelineDate.setDate1(simpleDateFormat.parse("15-12-2016 AD"));
        timelineDate.setDate2(simpleDateFormat.parse("20-12-2016 AD"));
        result.setTimelineDate(timelineDate);
        resultList.add(result);
        result = new Result();
        timelineDate = new TimelineDate();
        timelineDate.setDate1(simpleDateFormat.parse("15-12-2016 AD"));
        timelineDate.setDate2(simpleDateFormat.parse("20-12-2016 AD"));
        result.setTimelineDate(timelineDate);
        resultList.add(result);
        result = new Result();
        timelineDate = new TimelineDate();
        timelineDate.setDate1(simpleDateFormat.parse("01-01-0499 BC"));
        timelineDate.setDate2(simpleDateFormat.parse("31-12-9999 AD"));
        result.setTimelineDate(timelineDate);
        resultList.add(result);

        ProduceRanges produceRanges = new ProduceRanges();
        produceRanges.produceRanges(resultList);
        List<Range> actualTrees = produceRanges.getTrees();
        List<Range> expectedTrees = new ArrayList<>();
        Range range = new Range(simpleDateFormat.parse("01-01-0499 BC"), simpleDateFormat.parse("31-12-9999 AD"));
        Range range1 = new Range(simpleDateFormat.parse("01-01-0001 AD"), simpleDateFormat.parse("31-12-9999 AD"));
        Range range2 = new Range(simpleDateFormat.parse("12-12-2016 AD"), simpleDateFormat.parse("20-12-2016 AD"));
/*
        Range range3 = new Range(simpleDateFormat.parse(""))
*/
        range1.addChild(range2);
        range.addChild(range1);

    }
}
