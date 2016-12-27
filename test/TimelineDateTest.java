import org.junit.Assert;
import org.junit.Test;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Test for the parsing of Normalized Entity Tag in TimelineDate.
 */
public class TimelineDateTest {
    private SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd");
    //TODO: more complex dates to parse

    /**
     * Passes in a simple date of the format yyyy-MM-dd into TimelineDate, and checks the output date.
     *
     * @throws ParseException
     */
    @Test
    public void testTimelineDateProcessSimple() throws ParseException {
        String dateStr = "2016-12-24";
        Date expectedDate = simpleDateFormat.parse(dateStr);

        TimelineDate timelineDate = new TimelineDate();
        timelineDate.parse(dateStr);//should make a Date object of the same type, not much processing.

        Assert.assertEquals(expectedDate, timelineDate.getDate1());
        Assert.assertEquals(null, timelineDate.getDate2());//should have only picked up one date
    }
}
