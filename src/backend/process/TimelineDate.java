package backend.process;

import edu.stanford.nlp.util.Pair;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.regex.Pattern;

/**
 * Attempts to generate an exact date for an event, to then order the events.
 * Holds the start and end date (appropriately) for each event in the timeline. It updates as new dates, relevant to the
 */
//TODO: PRESENT_REF date means date now, check of date values are possible before trying to create (eg checking if month has 31 days). If just year-month should create range?
public class TimelineDate implements Comparable<TimelineDate> {
    private static final String year = "0001";
    private static final String month = "01";
    private static final String day = "01";
    private static final Map<String, Pair<String, String>> seasonMap;

    static {
        /*
            Seasons are given according to educationuk.org, by:
                Winter December , January, February (12,1,2)
                Spring is March, April and May (3,4,5)
                Summer is June, July, August (6,7,8)
                Autumn September, October, November (9,10,11)
         */
        Map<String, Pair<String, String>> map = new HashMap<>();
        map.put("WI", new Pair<>("12", "02"));
        map.put("SP", new Pair<>("03", "05"));
        map.put("SU", new Pair<>("06", "08"));
        map.put("FA", new Pair<>("09", "11"));
        seasonMap = map;
    }

    private final static Pattern onlyYearPattern = Pattern.compile("(\\d{4})|(\\d{3}X)|(\\d{2}XX)|(\\dXXX)|(XXXX)");
    private final static Pattern onlyMonthPattern = Pattern.compile("\\d{2}");
    private final static Pattern onlyWeekNumberPattern = Pattern.compile("W\\d{2}");
    private final static Pattern onlySeasonPattern = Pattern.compile("[A-Z]{2}");
    private final static Pattern onlyDayPattern = Pattern.compile("\\d{2}");
    private final static Pattern onlyPresentRefPattern = Pattern.compile(".*PRESENT_REF.*");
    private final static Pattern onlyBeforeYearPattern = Pattern.compile("(\\-\\d{4})|(\\-\\d{3}X)|(\\-\\d{2}XX)|(\\-\\dXXX)|(\\-XXXX)");
    private final static Pattern onlyWeekendPattern = Pattern.compile("WE");
    private Calendar calendar;
    private Date date1;//first (min, start) date
    private Date date2;//second (max, end) date
    private String dateStr;

    /**
     * Initialises the Calendar used to determine dates based on week number.
     */
    public TimelineDate() {
        calendar = Calendar.getInstance();
    }

    /**
     * Update the dates hold by this, based on the input text.
     *
     * @param date a date provided by the StanfordCoreNLP library: it is a normalized entity
     */
    public void parse(String date) {
        ArrayList<Date> dates = new ArrayList<>();
        //splitting INTERSECT
        String[] splitDate = date.split("INTERSECT");
        if (splitDate.length > 0) {// on the first part of the date, which is just a date, get its specfic date
            String possibleDates = splitDate[0];//this date could also be a range, ie include /
            String[] splitRange = possibleDates.split("/");
            for (String possibleDate : splitRange) {
                dates.addAll(getDate(possibleDate));// from processing the individual date, add it to dates
            }
        }
        //if we had INTERSECT then we should process it for additional info to show
        //if we have more than 2 dates in the list, then keep the minimum date and max date and remove all the others
        enforceRule(dates, date);
    }

    /**
     * For the given input, produce a list of dates based on it.
     *
     * @param date an input text that contains date information (can be exact or relative).
     * @return a list of exact Dates formed from the input.
     */
    private ArrayList<Date> getDate(String date) {
        calendar.clear();
        ArrayList<Date> dates = new ArrayList<>();
        String year1 = year;//setting the default values
        String month1 = month;
        String day1 = day;
        String year2 = null;
        String month2 = null;
        String day2 = null;
        //can have a date that is PRESENT_REF (when the text has now)
        if(onlyPresentRefPattern.matcher(date).matches()){
            //should be base date, as it means this current moment?
        }else if(onlyBeforeYearPattern.matcher(date).matches()){
            System.out.println("Matches BC pattern");
            //create this into a normalized date
        }
        //BC dates -dddd with Xs

        //else

        //now need to split date into its individual components
        String[] dateInfo = date.split("-");
        for (int i = 0; i < dateInfo.length; i++) {
            if (i == 0) {//this can only be a year
                //check year format
                if (onlyYearPattern.matcher(dateInfo[i]).matches()) {
                    year1 = dateInfo[i].replace("X", "0");
                    if (dateInfo[i].contains("X")) {//if we do have a range then we need to set the values for the second date
                        year2 = dateInfo[i].replace("X", "9");//could be the casse that we dont want it to point to the last day in the year
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
                    //calculate month and start day-end
                    //split W from actual week number
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
                if (onlyDayPattern.matcher(dateInfo[i]).matches()) {//got the day
                    day1 = dateInfo[i];
                }else if(onlyWeekendPattern.matcher(dateInfo[i]).matches()){//previously should have had week number so its a range
                    //checking its range has been set before
                    if(day1 != null && day2 != null && month1 != null && month2 != null) {
                        //then lets refine the dates that were just before a week long, now down to a weekend
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
        System.out.println("For: " + date);
        System.out.println("For Date1 we have: " + year1 + "-" + month1 + "-" + day1);
        System.out.println("For Date2 we have: " + year2 + "-" + month2 + "-" + day2);
        //trying to form date objects
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd");
        try {
            Date date1 = simpleDateFormat.parse(returnDate(year1, month1, day1));
            dates.add(date1);
            System.out.println(date1);
            if (year2 != null && month2 != null && day2 != null) {
                Date date2 = simpleDateFormat.parse(returnDate(year2, month2, day2));
                dates.add(date2);
                System.out.println(date2);
            }
        } catch (ParseException e) {
            //could not add the dates
        }
        System.out.println("\n");
        return dates;
    }

    /**
     * Used to find update the min/max dates held. Will update the dates held if a new min/max has been found.
     *
     * @param newDates a list of possible new min/max dates
     * @param date     the string that produced these dates.
     */
    private void enforceRule(ArrayList<Date> newDates, String date) {
        Collections.sort(newDates);
        Date date1 = newDates.get(0);
        Date date2 = null;
        if (newDates.size() > 1) {
            date2 = newDates.get(newDates.size() - 1);
        }
        if (this.date1 == null || this.date1.compareTo(date1) > 0) {//we found a new lowest date
            this.date1 = date1;
            dateStr = date;
        }
        if (date2 != null && (this.date2 == null || this.date2.compareTo(date2) < 0)) {//found a new max/highest date
            this.date2 = date2;
            dateStr = date;
        }
    }

    /**
     * Produces a date String of the format yyyy-MM-dd, for the given input.
     *
     * @param year  the year of the date
     * @param month the month of the date
     * @param day   the day of the date
     * @return a String of the format yyyy-MM-dd
     */
    private String returnDate(String year, String month, String day) {
        return String.format("%s-%s-%s", year, month, day);
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
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd");
        if (date1 != null) {
            toReturn += simpleDateFormat.format(date1);
        }
        if (date2 != null) {
            toReturn += " -> " + simpleDateFormat.format(date2);
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
        if (o.date1 != null && this.date1 != null) {
            return o.date1.compareTo(this.date1);
        }
        if (this.date1 == null) {
            return -1;
        }
        return 1;
    }

    /**
     * Checks whether a given backend.process.TimelineDate object is equal to this object.
     *
     * @param obj the other object we are comparing to.
     * @return true if the other backend.process.TimelineDate object has the same dates as this.
     */
    @Override
    public boolean equals(Object obj) {
        System.out.println("Comparing: " + this + " to " + obj);
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
    }

    /**
     * Set the Date for date2.
     *
     * @param date2 Date for date2.
     */
    public void setDate2(Date date2) {
        this.date2 = date2;
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
}
