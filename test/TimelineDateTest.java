import org.junit.Assert;
import org.junit.Test;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Created by Oliver on 23/12/2016.
 */
//TODO: pass in dates, and compare it to expected produced Dates
public class TimelineDateTest {
    private SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd");
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
