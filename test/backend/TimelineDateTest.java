package backend;

import backend.process.TimelineDate;
import org.junit.Assert;
import org.junit.Test;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Test for the parsing of Normalized Entity Tag in backend.process.TimelineDate.
 */
public class TimelineDateTest {
    private static final String PRESENT_REF = "PRESENT_REF";
    private SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd");
    private String baseDate = "2016-12-30";//doesn't affect the output of most tests, but has to be still passed in.

    /**
     * Passes in a simple date of the format yyyy-MM-dd into backend.process.TimelineDate, and checks the output date.
     *
     * @throws ParseException when comparing the expected Date to check.
     */
    @Test
    public void testTimelineDateProcessSimple() throws ParseException {
        String dateStr = "2016-12-24";
        Date expectedDate = simpleDateFormat.parse(dateStr);

        TimelineDate timelineDate = new TimelineDate();
        timelineDate.parse(dateStr, baseDate);//should make a Date object of the same type, not much processing.

        Assert.assertEquals(expectedDate, timelineDate.getDate1());
        Assert.assertEquals(null, timelineDate.getDate2());//should have only picked up one date
    }

    /**
     * Passes in a normalized entity tag for a year range, 198X, and checks the output to the expected start and end Date
     * of the year range.
     *
     * @throws ParseException when creating the expected Dates to check.
     */
    @Test
    public void testTimelineDateProcessYearRange() throws ParseException {
        String yearRange = "198X";
        String expectedStartDate = "1980-01-01";
        String expectedEndDate = "1989-12-31";

        Date expectedStart = simpleDateFormat.parse(expectedStartDate);
        Date expectedEnd = simpleDateFormat.parse(expectedEndDate);

        TimelineDate timelineDate = new TimelineDate();
        timelineDate.parse(yearRange, baseDate);

        Assert.assertEquals(expectedStart, timelineDate.getDate1());
        Assert.assertEquals(expectedEnd, timelineDate.getDate2());

    }

    /**
     * Passes in a normalized entity tag for an actual year range, 1980-01-01/2016-10-25, and checks the output to the
     * expected start and end Date.
     *
     * @throws ParseException when creating the expected Dates.
     */
    @Test
    public void testTimelineDateProcessActualDateRange() throws ParseException {
        String input = "1980-01-01/2016-10-25";
        String expectedStartDate = "1980-01-01";
        String expectedEndDate = "2016-10-25";

        Date expectedStart = simpleDateFormat.parse(expectedStartDate);
        Date expectedEnd = simpleDateFormat.parse(expectedEndDate);

        TimelineDate timelineDate = new TimelineDate();
        timelineDate.parse(input, baseDate);

        System.out.println(timelineDate);
        Assert.assertEquals(expectedStart, timelineDate.getDate1());
        Assert.assertEquals(expectedEnd, timelineDate.getDate2());
    }

    /**
     * Passes in a year and a season to backend.process.TimelineDate, and compares the output to an expected start and end Dates.
     *
     * @throws ParseException when creating the expected Dates.
     */
    @Test
    public void testTimelineDateProcessSeason() throws ParseException {
        String input = "1980-SP";
        String expectedStartDate = "1980-03-01";
        String expectedEndDate = "1980-05-31";

        Date expectedStart = simpleDateFormat.parse(expectedStartDate);
        Date expectedEnd = simpleDateFormat.parse(expectedEndDate);

        TimelineDate timelineDate = new TimelineDate();
        timelineDate.parse(input, baseDate);

        System.out.println(timelineDate);

        Assert.assertEquals(expectedStart, timelineDate.getDate1());
        Assert.assertEquals(expectedEnd, timelineDate.getDate2());
    }

    /**
     * Passes in a year with a week number to backend.process.TimelineDate, and compares the output to an expected start and end Dates.
     *
     * @throws ParseException when creating the expected Dates.
     */
    @Test
    public void testTimelineDateProcessWeekNumber() throws ParseException {
        String input = "2016-W47";
        String expectedStartDate = "2016-11-21";
        String expectedEndDate = "2016-11-27";

        Date expectedStart = simpleDateFormat.parse(expectedStartDate);
        Date expectedEnd = simpleDateFormat.parse(expectedEndDate);

        TimelineDate timelineDate = new TimelineDate();
        timelineDate.parse(input, baseDate);

        System.out.println(timelineDate);

        Assert.assertEquals(expectedStart, timelineDate.getDate1());
        Assert.assertEquals(expectedEnd, timelineDate.getDate2());
    }

    /**
     * Passes in a year and a month, and checks the output to an expected start Date.
     *
     * @throws ParseException when creating the expected Date.
     */
    @Test
    public void testTimelineDateProcessMonth() throws ParseException {
        String input = "2016-10";
        String expectedStartDate = "2016-10-01";

        Date expectedStart = simpleDateFormat.parse(expectedStartDate);

        TimelineDate timelineDate = new TimelineDate();
        timelineDate.parse(input, baseDate);

        System.out.println(timelineDate);

        Assert.assertEquals(expectedStart, timelineDate.getDate1());
    }

    /**
     * Tests the resulting date produced when a normalized entity tag for now (PRESENT_REF) is passed into TimelineDate.
     *
     * @throws ParseException when creating the expected Date.
     */
    @Test
    public void testTimelineDateProcessNow() throws ParseException {
        //the base date passed in should be the date used when it processes REFERENCE_NOW
        //base date being
        Date expectedDate = simpleDateFormat.parse(baseDate);

        TimelineDate timelineDate = new TimelineDate();
        timelineDate.parse("PRESENT_REF", baseDate);

        Assert.assertEquals(expectedDate, timelineDate.getDate1());
        Assert.assertEquals(null, timelineDate.getDate2());//shouldnt have a second date
    }

    /**
     * Tests the processing of the INTERSECT data (duration) that can come with a normalized entity tag for Dates.
     * Tests for a simple INTERSECT (4 Years).
     *
     * @throws ParseException when creating the expected Date.
     */
    @Test
    public void testTimelineDateProcessINTERSECT() throws ParseException {
        String expectedResultDuration = "Period: 4 Year(s)";
        Date expectedResultDate = simpleDateFormat.parse(baseDate);
        String input = baseDate + " INTERSECT P4Y";

        TimelineDate timelineDate = new TimelineDate();
        timelineDate.parse(input, baseDate);

        Assert.assertEquals(expectedResultDate, timelineDate.getDate1());
        Assert.assertEquals(null, timelineDate.getDate2());
        Assert.assertEquals(expectedResultDuration, timelineDate.getDurationData());
    }

    /**
     * Tests the processing of a more complex INTERSECT data (duration), including time.
     *
     * @throws ParseException when creating the expected Date.
     */
    @Test
    public void testTimelineDateProcessINTERSECTInclTime() throws ParseException {
        String expectedResultDuration = "Period: 3 Year(s) 6 Month(s) 4 Day(s) Time: 12 Hour(s) 30 Minute(s) 5 Second(s)";
        Date expectedResultDate = simpleDateFormat.parse(baseDate);
        String input = baseDate + " INTERSECT P3Y6M4DT12H30M5S";

        TimelineDate timelineDate = new TimelineDate();
        timelineDate.parse(input, baseDate);

        Assert.assertEquals(expectedResultDate, timelineDate.getDate1());
        Assert.assertEquals(null, timelineDate.getDate2());
        Assert.assertEquals(expectedResultDuration, timelineDate.getDurationData());

    }
}
