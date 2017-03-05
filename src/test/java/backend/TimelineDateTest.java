package backend;

import backend.process.TimelineDate;
import edu.stanford.nlp.time.SUTime;
import org.junit.Assert;
import org.junit.Test;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Test for the parsing of Normalized Entity Tag in backend.process.TimelineDate, and how they are processed.
 */
public class TimelineDateTest {
    private static final String PRESENT_REF = "PRESENT_REF";
    private static final String PAST_REF = "PAST_REF";
    private static final String FUTURE_REF = "FUTURE_REF";
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
        timelineDate.parse(PRESENT_REF, baseDate);

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

    /**
     * Tests the auto correct for Dates, where the day value has been over-estimated (that day value for that month
     * does not exist).
     *
     * @throws ParseException when creating the expected Date.
     */
    @Test
    public void testTimelineDateProcessAutoCorrectDates() throws ParseException {
        String inputWrongDate = "2016-12-32";
        String expectedDateStr = "2016-12-31";
        Date expectedDate = simpleDateFormat.parse(expectedDateStr);

        TimelineDate timelineDate = new TimelineDate();
        timelineDate.parse(inputWrongDate, baseDate);

        Assert.assertEquals(expectedDate, timelineDate.getDate1());
        Assert.assertEquals(null, timelineDate.getDate2());
    }

    /**
     * Tests the auto correct for Dates, where the day value has been over-estimated, such that the month value has to
     * also be updated.
     *
     * @throws ParseException when creating the expected Date.
     */
    @Test
    public void testTimelineDateProcessAutoCorrectDatesAndMonths() throws ParseException {
        String inputWrongDate = "2016-12-00";
        String expectedDateStr = "2016-11-30";
        Date expectedDate = simpleDateFormat.parse(expectedDateStr);

        TimelineDate timelineDate = new TimelineDate();
        timelineDate.parse(inputWrongDate, baseDate);

        Assert.assertEquals(expectedDate, timelineDate.getDate1());
        Assert.assertEquals(null, timelineDate.getDate2());
    }

    /**
     * Tests the processing of a full BC normalized entity Date, of the format -yyyy-MM-dd.
     *
     * @throws ParseException when creating the expected Date.
     */
    @Test
    public void testTimelineDateBCFullDate() throws ParseException {
        SimpleDateFormat simpleDateFormatBC = new SimpleDateFormat("yyyy-MM-dd G");
        String input = "-0004-12-31";
        String expectedDateStr = "0004-12-31 BC";
        Date expectedDate = simpleDateFormatBC.parse(expectedDateStr);

        TimelineDate timelineDate = new TimelineDate();
        timelineDate.parse(input, baseDate);

        Assert.assertEquals(expectedDate, timelineDate.getDate1());
        Assert.assertNull(timelineDate.getDate2());
    }

    /**
     * Tests the processing of only a BC year normalized entity Date, of the format -yyyy, to see how TimelineDate gives
     * it a Date value (i.e. assumes it means the start of the year, so month 01 and day 01)
     *
     * @throws ParseException when creating the expected Date.
     */
    @Test
    public void testTimelineDateBCYearDate() throws ParseException {
        SimpleDateFormat simpleDateFormatBC = new SimpleDateFormat("yyyy-MM-dd G");
        String input = "-0004";
        String expectedDateStr = "0004-01-01 BC";
        Date expectedDate = simpleDateFormatBC.parse(expectedDateStr);

        TimelineDate timelineDate = new TimelineDate();
        timelineDate.parse(input, baseDate);

        Assert.assertEquals(expectedDate, timelineDate.getDate1());
        Assert.assertNull(timelineDate.getDate2());
    }

    /**
     * Tests the processing of a BC Date, that has a day value that is wrong for that month. Looks at how this Date is
     * reduced until it correct (assuming we over-estimated the day value).
     *
     * @throws ParseException when creating the expected Date.
     */
    @Test
    public void testTimelineDateBCWrongDate() throws ParseException {
        SimpleDateFormat simpleDateFormatBC = new SimpleDateFormat("yyyy-MM-dd G");
        String input = "-0004-12-32";
        String expectedDateStr = "0004-12-31 BC";
        Date expectedDate = simpleDateFormatBC.parse(expectedDateStr);

        TimelineDate timelineDate = new TimelineDate();
        timelineDate.parse(input, baseDate);

        Assert.assertEquals(expectedDate, timelineDate.getDate1());
        Assert.assertNull(timelineDate.getDate2());
    }

    /**
     * Tests the building of a range of Dates, for a BC date. Testing how the system infers the start and end Dates for
     * BC dates. (Should be the same as AD dates, just that the year values to towards 0 as it is B.C.)
     *
     * @throws ParseException when creating the expected Date.
     */
    @Test
    public void testTimelineDateBCDateRange() throws ParseException {
        SimpleDateFormat simpleDateFormatBC = new SimpleDateFormat("yyyy-MM-dd G");
        String input = "-04XX-12-32";
        String expectedDateStr1 = "0499-12-31 BC";
        String expectedDateStr2 = "0400-12-31 BC";
        Date expectedDate1 = simpleDateFormatBC.parse(expectedDateStr1);
        Date expectedDate2 = simpleDateFormatBC.parse(expectedDateStr2);

        TimelineDate timelineDate = new TimelineDate();
        timelineDate.parse(input, baseDate);

        Assert.assertEquals(expectedDate1, timelineDate.getDate1());
        Assert.assertEquals(expectedDate2, timelineDate.getDate2());
    }

    /**
     * Tests the case when the input is a week number with the day of the week.
     *
     * @throws ParseException when the expected date is created.
     */
    @Test
    public void testWeekDate() throws ParseException {
        String input = "2016-W47-7";//the seventh day in week 47 in iso 8601 is the sunday
        String expectedDateStr = "2016-11-27";//this the actual date for sunday
        Date expectedDate = simpleDateFormat.parse(expectedDateStr);
        TimelineDate timelineDate = new TimelineDate();
        timelineDate.parse(input, baseDate);
        Assert.assertEquals(expectedDate, timelineDate.getDate1());
        Assert.assertEquals(null, timelineDate.getDate2());
    }

    /**
     * Tests that when a Past reference is passed, a range of dates from the start of AD years until the base date is
     * created.
     *
     * @throws ParseException when the expected dates are created.
     */
    @Test
    public void testPastRef() throws ParseException {
        String input = PAST_REF;
        String expectedDateStr1 = "0001-01-01";
        String expectedDateStr2 = baseDate;//past ref points from start of year in AD to base date
        Date expectedDate1 = simpleDateFormat.parse(expectedDateStr1);
        Date expectedDate2 = simpleDateFormat.parse(expectedDateStr2);
        TimelineDate timelineDate = new TimelineDate();
        timelineDate.parse(input, baseDate);
        Assert.assertEquals(expectedDate1, timelineDate.getDate1());
        Assert.assertEquals(expectedDate2, timelineDate.getDate2());
    }

    /**
     * Tests that when a Future reference is passed, a range of dates from the base date until the end of times is
     * created.
     *
     * @throws ParseException when creating the expected dates.
     */
    @Test
    public void testFutureRef() throws ParseException {
        String input = FUTURE_REF;
        String expectedDateStr1 = baseDate;
        String expectedDateStr2 = "9999-12-31";
        Date expectedDate1 = simpleDateFormat.parse(expectedDateStr1);
        Date expectedDate2 = simpleDateFormat.parse(expectedDateStr2);
        TimelineDate timelineDate = new TimelineDate();
        timelineDate.parse(input, baseDate);
        Assert.assertEquals(expectedDate1, timelineDate.getDate1());
        Assert.assertEquals(expectedDate2, timelineDate.getDate2());
    }

    /**
     * Tests that TimelineDate enforces the Rules of expanding the Range of dates.
     * I.e. when pass a date which is earlier than the current date1, then date1 should be updated, or if a date is
     * passed in that is further away in the future than date2, then date2 should be updated. However, if a date is
     * passed in that is within the range, then the date1, and date2 values should not be changed.
     *
     * @throws ParseException when creating the expected Dates.
     */
    @Test
    public void testEnforceRule() throws ParseException {
        String input = baseDate;
        Date expectedDate1 = simpleDateFormat.parse(input);
        Date expectedDate2 = null;
        TimelineDate timelineDate = new TimelineDate();
        timelineDate.parse(input, baseDate);
        Assert.assertEquals(expectedDate1, timelineDate.getDate1());
        Assert.assertNull(timelineDate.getDate2());
        input = "2015-12-15";
        timelineDate.parse(input, baseDate);
        expectedDate1 = simpleDateFormat.parse(input);
        Assert.assertEquals(expectedDate1, timelineDate.getDate1());
        Assert.assertNull(timelineDate.getDate2());
        input = "2077-01-05";
        timelineDate.parse(input, baseDate);
        expectedDate2 = simpleDateFormat.parse(input);
        Assert.assertEquals(expectedDate1, timelineDate.getDate1());
        Assert.assertEquals(expectedDate2, timelineDate.getDate2());
        input = "2055-12-24";
        timelineDate.parse(input, baseDate);//dates shouldnt have changed as range hasnt expanded
        Assert.assertEquals(expectedDate1, timelineDate.getDate1());
        Assert.assertEquals(expectedDate2, timelineDate.getDate2());
    }

}
