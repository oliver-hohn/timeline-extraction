package backend;

import backend.helpers.ToJSON;
import backend.process.FileData;
import backend.process.Result;
import backend.process.TimelineDate;
import org.junit.Assert;
import org.junit.Test;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;

/**
 * Test Class to test the toJSON class (ie taking a list of Results and producing JSON Strings).
 */
public class ToJSONTest {
    private static final SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd G");

    /**
     * Tests the JSON String produced for a List of one Result object which has all the data initialised (i.e. all the
     * fields in the JSON should be populated).
     *
     * @throws ParseException when producing the dates (date1, date2 from TimelineDate) for the test Result object.
     */
    @Test
    public void testCompleteJSON() throws ParseException {
        Result result = new Result();
        TimelineDate timelineDate = new TimelineDate();
        timelineDate.setDate1(simpleDateFormat.parse(returnDate("2016", "02", "14", false)));
        timelineDate.setDate2(simpleDateFormat.parse(returnDate("2016", "02", "15", false)));
        result.setTimelineDate(timelineDate);
        result.addSubject("Valentines Day");
        result.addSubject("Party");
        result.setEvent("On Valentines Day we had a huge party!");
        FileData fileData = new FileData("party.txt", "FAKEPATH");
        fileData.setCreationDate("02-02-2017");
        result.setFileData(fileData);

        ArrayList<Result> results = new ArrayList<>();
        results.add(result);
        //process the full result

        String actualResultJson = ToJSON.toJSON(results);
        String expectedJson = "[{\"date1\":\"14-02-2016 AD\",\"date2\":\"15-02-2016 AD\",\"subjects\":[\"Valentines Day\",\"Party\"],\"event\":\"On Valentines Day we had a huge party!\",\"from\":{\"filename\":\"party.txt\",\"baseDate\":\"02-02-2017\"}}]";
        Assert.assertEquals(expectedJson, actualResultJson);
    }

    /**
     * Tests the JSON String produced for a List of one Result object, where only some of its data fields have been
     * populated (date1, event, and from fields should have data).
     *
     * @throws ParseException when producing the date1 in TimelineDate for the test Result object.
     */
    @Test
    public void testPartialJSON() throws ParseException {
        Result result = new Result();
        TimelineDate timelineDate = new TimelineDate();
        timelineDate.setDate1(simpleDateFormat.parse(returnDate("2017", "02", "14", false)));
        result.setTimelineDate(timelineDate);
        result.setEvent("On Valentines Day we had a huge party!");
        FileData fileData = new FileData("party.txt", "FAKEPATH");
        fileData.setCreationDate("02-02-2017");
        result.setFileData(fileData);

        ArrayList<Result> results = new ArrayList<>();
        results.add(result);

        String actualResultJson = ToJSON.toJSON(results);
        String expectedJson = "[{\"date1\":\"14-02-2017 AD\",\"subjects\":[],\"event\":\"On Valentines Day we had a huge party!\",\"from\":{\"filename\":\"party.txt\",\"baseDate\":\"02-02-2017\"}}]";
        Assert.assertEquals(expectedJson, actualResultJson);
    }

    /**
     * Tests the JSON String produced for a List of two Result objects, where one is fully populated (i.e. all fields
     * have non null values) and the other is partially populated (i.e. only date1, event and from fields are populated).
     *
     * @throws ParseException when producing the date1 and/or date2 in TimelineDate for the test Result objects (since
     *                        its a list).
     */
    @Test
    public void testMultipleResultJSON() throws ParseException {
        ArrayList<Result> results = new ArrayList<>();

        Result result = new Result();
        TimelineDate timelineDate = new TimelineDate();
        timelineDate.setDate1(simpleDateFormat.parse(returnDate("2016", "02", "14", false)));
        timelineDate.setDate2(simpleDateFormat.parse(returnDate("2016", "02", "15", false)));
        result.setTimelineDate(timelineDate);
        result.addSubject("Valentines Day");
        result.addSubject("Party");
        result.setEvent("On Valentines Day we had a huge party!");
        FileData fileData = new FileData("party.txt", "FAKEPATH");
        fileData.setCreationDate("02-02-2017");
        result.setFileData(fileData);
        results.add(result);

        result = new Result();
        timelineDate = new TimelineDate();
        timelineDate.setDate1(simpleDateFormat.parse(returnDate("2017", "02", "14", false)));
        result.setTimelineDate(timelineDate);
        result.setEvent("On Valentines Day we had a huge party!");
        fileData = new FileData("party.txt", "FAKEPATH");
        fileData.setCreationDate("02-02-2017");
        result.setFileData(fileData);
        results.add(result);

        String actualResultsJson = ToJSON.toJSON(results);
        String expectedJson = "[{\"date1\":\"14-02-2016 AD\",\"date2\":\"15-02-2016 AD\",\"subjects\":[\"Valentines Day\",\"Party\"],\"event\":\"On Valentines Day we had a huge party!\",\"from\":{\"filename\":\"party.txt\",\"baseDate\":\"02-02-2017\"}},{\"date1\":\"14-02-2017 AD\",\"subjects\":[],\"event\":\"On Valentines Day we had a huge party!\",\"from\":{\"filename\":\"party.txt\",\"baseDate\":\"02-02-2017\"}}]";
        Assert.assertEquals(expectedJson, actualResultsJson);
    }

    /**
     * Tests the JSON String produced for a List of one Result object, that has just been initialised, and had no data
     * set to it.
     */
    @Test
    public void testBareResult() {
        Result result = new Result();
        ArrayList<Result> results = new ArrayList<>();
        results.add(result);

        String actualResultJson = ToJSON.toJSON(results);
        String expectedJson = "[{\"subjects\":[],\"event\":\"\",\"from\":{}}]";
        Assert.assertEquals(expectedJson, actualResultJson);
    }

    /**
     * Tests the JSON String produced for an empty List.
     */
    @Test
    public void testNoResults() {
        ArrayList<Result> results = new ArrayList<>();
        String actualResultJson = ToJSON.toJSON(results);
        String expectedJson = "[]";
        Assert.assertEquals(expectedJson, actualResultJson);
    }

    /**
     * Produces a date String of the format yyyy-MM-dd, for the given input.
     *
     * @param year  the year of the date
     * @param month the month of the date
     * @param day   the day of the date
     * @return a String of the format yyyy-MM-dd
     */
    private String returnDate(String year, String month, String day, boolean isBC) {
        if (isBC) {
            return String.format("%s-%s-%s BC", year, month, day);
        }
        return String.format("%s-%s-%s AD", year, month, day);
    }

}
