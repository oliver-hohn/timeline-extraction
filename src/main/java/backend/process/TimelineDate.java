package backend.process;

import edu.stanford.nlp.util.Pair;
import org.joda.time.DateTime;
import org.joda.time.Days;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.regex.Pattern;

/**
 * Attempts to generate an exact date for an event, to then order the events.
 * Holds the start and end date (appropriately) for each event in the timeline. It updates as new dates, relevant to the
 */
//If just year-month should create range?
public class TimelineDate implements Comparable<TimelineDate> {
    private static final String year = "0001";
    private static final String month = "01";
    private static final String day = "01";
    private static final Map<String, Pair<String, String>> seasonMap;
    private static final Map<Character, String> durationMap;
    private static final Map<Character, String> timeMap;
    private final SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd G");
    private final SimpleDateFormat dayMonthYearFormat = new SimpleDateFormat("dd-MM-yyyy G");

    static {
        /*
            Seasons are given according to educationuk.org, by:
                Winter December , January, February (12,1,2)
                Spring is March, April and May (3,4,5)
                Summer is June, July, August (6,7,8)
                Autumn September, October, November (9,10,11)
         */
        Map<String, Pair<String, String>> seasonM = new HashMap<>();
        seasonM.put("WI", new Pair<>("12", "02"));
        seasonM.put("SP", new Pair<>("03", "05"));
        seasonM.put("SU", new Pair<>("06", "08"));
        seasonM.put("FA", new Pair<>("09", "11"));
        seasonMap = seasonM;

        //durationMap
        Map<Character, String> durationM = new HashMap<>();
        durationM.put('P', "Period:");
        durationM.put('Y', "Year(s)");
        durationM.put('M', "Month(s)");
        durationM.put('W', "Week(s)");
        durationM.put('D', "Day(s)");
        durationMap = durationM;

        //timeMap
        Map<Character, String> timeM = new HashMap<>();
        timeM.put('T', "Time:");
        timeM.put('H', "Hour(s)");
        timeM.put('M', "Minute(s)");
        timeM.put('S', "Second(s)");
        timeMap = timeM;
    }

    //patterns are thread safe
    private final static Pattern onlyYearPattern = Pattern.compile("(\\d{4})|(\\d{3}X)|(\\d{2}XX)|(\\dXXX)|(XXXX)");
    private final static Pattern onlyMonthPattern = Pattern.compile("\\d{2}");
    private final static Pattern onlyWeekNumberPattern = Pattern.compile("W\\d{2}");
    private final static Pattern onlySeasonPattern = Pattern.compile("[A-Z]{2}");
    private final static Pattern onlyDayPattern = Pattern.compile("\\d{2}.*");
    private final static Pattern onlyPresentRefPattern = Pattern.compile(".*PRESENT_REF.*");
    private final static Pattern onlyPastRefPattern = Pattern.compile(".*PAST_REF.*");
    private final static Pattern onlyFutureRefPattern = Pattern.compile(".*FUTURE_REF.*");
    private final static Pattern onlyBeforeYearPattern = Pattern.compile("(\\-\\d{4})|(\\-\\d{3}X)|(\\-\\d{2}XX)|(\\-\\dXXX)|(\\-XXXX)");
    private final static Pattern onlyWeekendPattern = Pattern.compile("WE");
    private final static Pattern yearMonthDayPattern = Pattern.compile("\\d{4}\\-\\d{2}\\-\\d{2}");
    private Calendar calendar;
    private Date date1;//first (min, start) date
    private Date date2;//second (max, end) date
    private String dateStr;
    private String baseDate;
    private String durationData;//holds the latest duration data (additional info to show with event)
    //have a pair of list dates and duration string, if you use the dates pass in the string as additional info
    private int range = -1;

    /**
     * Initialises the Calendar used to determine dates based on week number.
     */
    public TimelineDate() {
        calendar = Calendar.getInstance();
        simpleDateFormat.setLenient(false);//can only create correct dates
    }

    /**
     * Update the dates hold by this, based on the input text.
     *
     * @param date a date provided by the StanfordCoreNLP library: it is a normalized entity
     */
    public void parse(String date, String baseDate) {
        System.out.println("Input: " + date);
        Pair<ArrayList<Date>, String> dateDurationPair = null;
        ArrayList<Date> dates = new ArrayList<>();
        String durationData = null;
        this.baseDate = baseDate;
        //splitting INTERSECT
        String[] splitDate = date.split("INTERSECT");
        if (splitDate.length > 0) {// on the first part of the date, which is just a date, get its specfic date
            String possibleDates = splitDate[0];//this date could also be a range, ie include /
            String[] splitRange = possibleDates.split("/");
            for (String possibleDate : splitRange) {
                dates.addAll(getDate(possibleDate.trim()));// from processing the individual date, add it to dates
            }
            //process INTERSECT data
            if (splitDate.length > 1) {
                String trimmedDuration = splitDate[1].trim();
                System.out.println("Processing: " + trimmedDuration);
                //process trimmed part which contains the duration data
                durationData = processINTERSECT(trimmedDuration);
            }
            //resulting list of dates and duration data should be put in a pair that is processed, where durationData
            //is only set if one of our dates are set
            dateDurationPair = new Pair<>(dates, durationData);
        }
        //if we had INTERSECT then we should process it for additional info to show
        //if we have more than 2 dates in the list, then keep the minimum date and max date and remove all the others
        enforceRule(dateDurationPair, date);
    }

    /**
     * Processes the INTERSECT data (duration of an event) provided sometimes with Date normalized entity tags.
     * Based on the ISO Standard 8601
     *
     * @param intersectData the data after "INTERSECT" in the normalized Date entity tags. Should start with 'P'.
     * @return a technical String representation of that data based on the ISO Standard 8601, i.e. Period X Year(s) Y Day(s)...
     */
    private String processINTERSECT(String intersectData) {
        String toReturn = "";
        //input starts with P
        char[] dataSplit = intersectData.toCharArray();
        if (dataSplit.length > 0 && dataSplit[0] == 'P') {
            System.out.println("Entered Period");
            for (int i = 0; i < dataSplit.length; i++) {
                char info = dataSplit[i];
                System.out.println(info);
                //when find T (go to time process method, once returned from that break)
                if (info == 'T') {
                    //go time method, and what it returns add toReturn
                    //make string from here till end of data
                    String hereToEnd = intersectData.substring(i, intersectData.length());
                    System.out.println("End Data: " + hereToEnd);
                    String toAdd = processTime(hereToEnd);
                    System.out.println("Adding: " + toAdd);
                    toReturn += toAdd;
                    break;//not processing the rest of the array as its character data
                }
                String fullText = ((durationMap.get(info) != null) ? " " + durationMap.get(info) + " " : info + "");
                System.out.println("Found: " + fullText);

                toReturn += fullText;
            }
            System.out.println("Every: " + toReturn.trim());
        }
        return toReturn.trim();
    }

    /**
     * Processes the Time duration part of the INTERSECT data after a Data has been given a normalized entity tag.
     * Based on the ISO Standard 8601.
     *
     * @param timeData time data that starts with 'T', based on ISO Standard 8601.
     * @return a technical String representation of the data passed in, i.e. Time X Hour(s) Y Minute(s)...
     */
    private String processTime(String timeData) {
        String toReturn = "";
        char[] splitTimeData = timeData.toCharArray();
        if (splitTimeData.length > 0 && splitTimeData[0] == 'T') {
            System.out.println("Processing Time data");
            for (char charTime : splitTimeData) {
                System.out.println("Processing: " + charTime);
                String fullText = ((timeMap.get(charTime) != null) ? " " + timeMap.get(charTime) + " " : charTime + "");
                System.out.println("Found: " + fullText);
                toReturn += fullText;
            }
            System.out.println(toReturn.trim());
        }
        return toReturn.trim();
    }

    /**
     * For the given input, produce a list of dates based on it.
     * Based on the ISO Standard 8601.
     *
     * @param date an input text that contains date information (can be exact or relative).
     * @return a list of exact Dates formed from the input.
     */
    private ArrayList<Date> getDate(String date) {
        calendar.clear();
        ArrayList<Date> toReturn = new ArrayList<>();
        String year1 = year;
        String month1 = month;
        String day1 = day;
        String year2 = null;
        String month2 = null;
        String day2 = null;
        boolean isBC = false;
        System.out.println("Trying to match: " + date);
        //need to check if its a PRESENT_REF, FUTURE_REF or PAST_REF
        if (onlyPastRefPattern.matcher(date).matches()) {
            System.out.println("PAST REF");
            //past so make range from 0001-01-01 -> base date (range)
            if (yearMonthDayPattern.matcher(baseDate).matches()) {
                //base date has the format yyyy-MM-dd
                String[] splitBaseDate = baseDate.split("-");
                //year1, month1, day1 values stay with default value
                year2 = splitBaseDate[0];
                month2 = splitBaseDate[1];
                day2 = splitBaseDate[2];//safe as pattern matched
            }
        } else if (onlyPresentRefPattern.matcher(date).matches()) {
            System.out.println("PRESENT REF");
            //use the base date (single date)
            if (yearMonthDayPattern.matcher(baseDate).matches()) {
                System.out.println("BaseDate matches: " + baseDate);
                String[] splitDate = baseDate.split("-");//so its safe to split it into 3 parts as pattern matched above
                year1 = splitDate[0];
                month1 = splitDate[1];
                day1 = splitDate[2];
            }
        } else if (onlyFutureRefPattern.matcher(date).matches()) {
            System.out.println("FUTURE REF");
            //future, from now til the last date we allow 9999-12-31 (range)
            if (yearMonthDayPattern.matcher(baseDate).matches()) {
                System.out.println("BaseDate matches: " + baseDate);
                String[] splitDate = baseDate.split("-");//so its safe to split it into 3 parts as pattern matched above
                year1 = splitDate[0];
                month1 = splitDate[1];
                day1 = splitDate[2];
            }
            year2 = "9999";//future point
            month2 = "12";
            day2 = "31";
        } else {
            System.out.println("ELSE");
            //else, need to check whether it is BC or AD
            if (date.length() >= 5 && onlyBeforeYearPattern.matcher(date.substring(0, 5)).matches()) {//got a negative date
                System.out.println("Past Date");
                isBC = true;
                date = date.substring(1, date.length());//removed - sign infront of year
            }
            //then calculate date
            boolean isWeekNumber = false;
            String[] dateInfo = date.split("-");
            for (int i = 0; i < dateInfo.length; i++) {
                if (i == 0) {//this can only be a year
                    //check year format
                    if (onlyYearPattern.matcher(dateInfo[i]).matches()) {
                        if (isBC) {
                            year1 = dateInfo[i].replace("X", "9");
                        } else {
                            year1 = dateInfo[i].replace("X", "0");
                            //check if its all 0000, then its BC
                            if (year1.equals("0000")) {
                                year1 = "0001";//we will start from year 1 (AD)
                            }
                        }
                        if (dateInfo[i].contains("X")) {//if we do have a range then we need to set the values for the second date
                            if (isBC) {
                                year2 = dateInfo[i].replace("X", "0");
                            } else {
                                year2 = dateInfo[i].replace("X", "9");//could be the case that we dont want it to point to the last day in the year

                            }
                            month2 = "12";//last day of the second year
                            day2 = "31";//assuming a range for 1980s means 1980 to the last day of 1989 (maximum possible range)
                            //could have every day in january, would want it to end in january?
                        }
                    }
                } else if (i == 1) {//this can be a week number, a month number or a season
                    //checking if its a month
                    System.out.println("Checking: " + dateInfo[i]);
                    if (onlyMonthPattern.matcher(dateInfo[i]).matches()) {
                        System.out.println("In onlyMonthPattern");
                        month1 = dateInfo[i];
                    } else if (onlyWeekNumberPattern.matcher(dateInfo[i]).matches()) {//checking if its a week number
                        isWeekNumber = true;
                        //calculate month and start day-end
                        //split W from actual week number
                        if (isBC) {
                            calendar.set(Calendar.ERA, GregorianCalendar.BC);
                        } else {
                            calendar.set(Calendar.ERA, GregorianCalendar.AD);
                        }
                        String weekNumber = dateInfo[i].substring(1);//W is the first part of the string, after it is the week number
                        calendar.set(Calendar.YEAR, getInt(year1));//should be set from previously
                        calendar.set(Calendar.WEEK_OF_YEAR, getInt(weekNumber));
                        calendar.set(Calendar.DAY_OF_WEEK, Calendar.MONDAY);//assuming we start on monday and end on sunday
                        year1 = new SimpleDateFormat("yyyy").format(calendar.getTime());//in the case the week goes into the next year, update our year1
                        month1 = new SimpleDateFormat("MM").format(calendar.getTime());
                        day1 = new SimpleDateFormat("dd").format(calendar.getTime());
                        //now for the end of the week
                        calendar.set(Calendar.DAY_OF_WEEK, Calendar.SUNDAY);
                        month2 = new SimpleDateFormat("MM").format(calendar.getTime());
                        day2 = new SimpleDateFormat("dd").format(calendar.getTime());
                        year2 = new SimpleDateFormat("yyyy").format(calendar.getTime());
                    } else if (onlySeasonPattern.matcher(dateInfo[i]).matches()) {//checking if its a season
                        String season = dateInfo[i];
                        Pair<String, String> seasonPair = seasonMap.get(season);//get the start and end month for the season
                        if (seasonPair != null) {//year1 should be set previously
                            //not quite sure what to do with BC here
                            month1 = seasonPair.first;
                            month2 = seasonPair.second;
                            day2 = "31";//set the second day as we are using a range
                            if (seasonPair.first.equals("12")) {//move onto the next year, so year2 should be updated
                                year2 = (getInt(year1) + 1) + "";
                            } else {//we dont need to increment the year as the season is within the same year
                                year2 = year1;
                            }
                        }
                    }
                } else if (i == 2) {//can be a day, or previously had week this could be weekend
                    System.out.println("Checking: " + dateInfo[i] + "size: " + dateInfo[i].length());
                    if (isWeekNumber) {//then if it has another digit, its the day of the week (so its not a week, but a specific day of the week)
                        System.out.println("Day of the week: " + dateInfo[i]);
                        int day = getInt(dateInfo[i]);
                        //as the calendar here starts with sunday and in iso it starts with monday, we need to increase by one and mod
                        day = (day % 7) + 1;
                        calendar.set(Calendar.DAY_OF_WEEK, day);
                        year1 = new SimpleDateFormat("yyyy").format(calendar.getTime());//in the case the week goes into the next year, update our year1
                        month1 = new SimpleDateFormat("MM").format(calendar.getTime());
                        day1 = new SimpleDateFormat("dd").format(calendar.getTime());
                        day2 = null;
                        month2 = null;
                        year2 = null;
                    }
                    if (onlyDayPattern.matcher(dateInfo[i]).matches()) {//got the day
                        System.out.println("Got day: " + dateInfo[i]);
                        day1 = dateInfo[i].substring(0, 2);
                    } else if (onlyWeekendPattern.matcher(dateInfo[i]).matches()) {//previously should have had week number so its a range
                        //checking its range has been set before
                        if (day1 != null && day2 != null && month1 != null && month2 != null) {
                            //then lets refine the dates that were just before a week long, now down to a weekend
                            if (isBC) {
                                calendar.set(Calendar.ERA, GregorianCalendar.BC);
                            } else {
                                calendar.set(Calendar.ERA, GregorianCalendar.AD);
                            }
                            calendar.set(Calendar.DAY_OF_WEEK, Calendar.SATURDAY);
                            year1 = new SimpleDateFormat("yyyy").format(calendar.getTime());//in the case the week goes into the next year, update our year1
                            month1 = new SimpleDateFormat("MM").format(calendar.getTime());
                            day1 = new SimpleDateFormat("dd").format(calendar.getTime());
                            calendar.set(Calendar.DAY_OF_WEEK, Calendar.SUNDAY);
                            month2 = new SimpleDateFormat("MM").format(calendar.getTime());
                            day2 = new SimpleDateFormat("dd").format(calendar.getTime());
                            year2 = new SimpleDateFormat("yyyy").format(calendar.getTime());
                        }
                    }
                }
            }
        }
        //make the dates
        Date date1;
        date1 = createDates(year1, month1, day1, isBC);
        toReturn.add(date1);
        System.out.println(date1);
        if (year2 != null && month2 != null && day2 != null) {
            Date date2 = createDates(year2, month2, day2, isBC);
            toReturn.add(date2);
            System.out.println(date2);
        }
        return toReturn;
    }

    /**
     * Creates a Data with the given input data. In the worst case we overestimated the day value (i.e. 31 when for
     * that month it can be 30), so we check that it is a legal date. If it isn't, then we reduce the day value,until we
     * get a correct day value. This is assuming that only the date values are wrong not month and years(these are given
     * by normalized entity tags, which should be correct according to SUTime annotator)
     *
     * @param year  the year value for this Date
     * @param month the month value for this Date
     * @param day   the day value for this Date (which can be wrong, so we reduce it until its right, assuming we always overestimate.
     * @param isBC  whether or not this is a BC or AD date; true if it is a BC Date.
     * @return a correct Date object based on the data passed in.
     */
    private Date createDates(String year, String month, String day, boolean isBC) {
        Date toReturn;
        String date = returnDate(year, month, day, isBC);
        System.out.println("Trying to create date for: " + date);
        try {
            toReturn = simpleDateFormat.parse(date);
        } catch (ParseException e) {
            //e.printStackTrace();//could comment this out
            System.out.println("Couldnt create date, so trying for a lower value");
            //couldnt create date, so most likely day value is to high, so reduce it
            int dayUpdate = Integer.parseInt(day);
            int monthUpdate;
            dayUpdate--;
            //should update month and year if fall below a certain threshold?
            if (dayUpdate <= 0) {//should decrease month val and set dayUpdate to 31
                dayUpdate = 31;
                System.out.println("Our day value fell below threshold, so go to month before this");
                monthUpdate = Integer.parseInt(month);
                monthUpdate--;//assuming we will never produce a month  below 1
                //which makes sense, as we only ever over estimate day values
                toReturn = createDates(year, Integer.toString(monthUpdate), Integer.toString(dayUpdate), isBC);
            } else {
                //update day value, so try again with this value
                toReturn = createDates(year, month, Integer.toString(dayUpdate), isBC);
            }
        }
        System.out.println("Created date for: " + toReturn);
        return toReturn;
    }

    /**
     * Used to find update the min/max dates held. Will update the dates held if a new min/max has been found.
     * MinMax Algorithm.
     *
     * @param dateDurationPair a Pair that has a list of possible new min/max dates, and their corresponding duration data (which can be null).
     * @param date             the string that produced these dates.
     */
    private void enforceRule(Pair<ArrayList<Date>, String> dateDurationPair, String date) {
        if (dateDurationPair != null) {
            ArrayList<Date> newDates = dateDurationPair.first();
            for (Date newDate : newDates) {
                if (this.date1 == null || this.date1.compareTo(newDate) > 0) {//if we dont have a date1, or we have a smaller one
                    this.date1 = newDate;
                    dateStr = date;
                    //else, we found a newDate that we havent set that is bigger than date1, look at date2
                    durationData = dateDurationPair.second();
                } else if ((this.date2 == null || this.date2.compareTo(newDate) < 0) && !this.date1.equals(newDate)) {//if we dont have date2, or we found a bigger date
                    this.date2 = newDate;                       //and the new date is not the first date
                    dateStr = date;
                    durationData = dateDurationPair.second();
                }
            }
            updateRange();
        }
    }

    /**
     * Updates the int value of the range based on the date1 and date2 values. If date2 is null then the range is 0, ie
     * the size of the range of dates is 0.
     */
    private void updateRange() {
        if (date1 != null && date2 != null) {
            DateTime dateTime1 = new DateTime(date1);
            DateTime dateTime2 = new DateTime(date2);
            range = Days.daysBetween(dateTime1, dateTime2).getDays();
        } else {
            range = 0;
        }
    }

    /**
     * Get the Range, ie the number of days between date1 and date2.
     *
     * @return the number of days between date1 and date2 (0 if date2 == null)
     */
    public int getRange() {
        updateRange();
        return range;
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

    /**
     * On the given input, produce an integer.
     *
     * @param number a number in String form.
     * @return the integer value corresponding to the string, or 1 if it is not possible to produce an int.
     */
    private int getInt(String number) {
        try {
            return Integer.parseInt(number);
        } catch (Exception e) {
            return 1;
        }
    }

    /**
     * @return date1 -> date2, or just date1 if we don't have a date2
     */
    @Override
    public String toString() {
        String toReturn = "";
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd G");
        if (date1 != null) {
            toReturn += simpleDateFormat.format(date1);
        }
        if (date2 != null) {
            toReturn += " -> " + simpleDateFormat.format(date2);
        }
        if (durationData != null) {
            toReturn += String.format(" (%s)", durationData);
        }
        return toReturn;
    }

    /**
     * Compares two TimelineDates based on their start date.
     *
     * @param o the other backend.process.TimelineDate that is being compared to.
     * @return the comparison of this date1 to the other's date1, or -1 if the other result does not have a date1, or 1 if this does not have a date1.
     */
    @Override
    public int compareTo(TimelineDate o) {
        if (range == -1) {
            updateRange();
        }
/*        if (o.date1 != null && this.date1 != null) {
            return o.date1.compareTo(this.date1);
        }
        if (this.date1 == null) {
            return -1;
        }
        return 1;*/
        if (range > o.range) {
            return 1;
        } else if (range < o.range) {
            return -1;
        } else {
            return 0;
        }
    }

    /**
     * Checks whether a given backend.process.TimelineDate object is equal to this object.
     *
     * @param obj the other object we are comparing to.
     * @return true if the other backend.process.TimelineDate object has the same dates as this.
     */
    @Override
    public boolean equals(Object obj) {
        boolean toReturn;
        try {
            TimelineDate other = (TimelineDate) obj;
            toReturn = date1.equals(other.date1);
            if (toReturn) {
                if (date2 != null || other.date2 != null) {//if we have a scond date then so should other, if we dont then they shouldnt either
                    toReturn = date2.equals(other.date2);
                }
            }
        } catch (Exception e) {
            return false;
        }
        return toReturn;
    }

    /**
     * Set the Date for date1.
     *
     * @param date1 Date for date1.
     */
    public void setDate1(Date date1) {
        this.date1 = date1;
        updateRange();
    }

    /**
     * Set the Date for date2.
     *
     * @param date2 Date for date2.
     */
    public void setDate2(Date date2) {
        this.date2 = date2;
        updateRange();
    }

    /**
     * Get the Date for date1.
     *
     * @return Date for date1.
     */
    public Date getDate1() {
        return date1;
    }

    /**
     * Get the Date for date2.
     *
     * @return Date for date2.
     */
    public Date getDate2() {
        return date2;
    }

    /**
     * Get the additional duration data for a date.
     *
     * @return String representing the duration an event occurred for (every when it repeated).
     */
    public String getDurationData() {
        return durationData;
    }

    /**
     * Get date1 as a String in the format dd-MM-yyyy G, or null if that is not possible.
     *
     * @return a formatted String of date1.
     */
    public String getDate1FormattedDayMonthYear() {
        try {
            return (date1 != null) ? dayMonthYearFormat.format(date1) : null;
        } catch (Exception e) {
            return null;
        }
    }

    /**
     * Get date2 as a String in the format dd-MM-yyyy G, or null if that is not possible.
     *
     * @return a formatted String of date2.
     */
    public String getDate2FormattedDayMonthYear() {
        try {
            return (date2 != null) ? dayMonthYearFormat.format(date2) : null;
        } catch (Exception e) {
            return null;
        }
    }
}
