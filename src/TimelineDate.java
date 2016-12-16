import edu.stanford.nlp.util.Pair;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.regex.Pattern;

/**
 * Represents the dates for a timeline event.
 * Months and days start from 1
 *
 *
 * stores passed in date as toString
 * generates an exact date to order
 *  TODO: method for addding to this timelinedate (when we already generated exact dates), store the list
 */
public class TimelineDate {
    private static final String year = "0001";
    private static final String month = "01";
    private static final String day = "01";
    private static final Map<String, Pair<Integer,Integer>> seasonMap;
    static {
        Map<String, Pair<Integer,Integer>> map = new HashMap<>();
        map.put("WI", new Pair<>(0, 2));
        map.put("SP", new Pair<>(3, 5));
        map.put("SU", new Pair<>(6, 8));
        map.put("FA", new Pair<>(9, 11));
        seasonMap = map;
    }
                                                                    //wont pick up for dates like: every year in january, which has format XXXX-01
    private final static Pattern yearPattern = Pattern.compile("(\\d{4}.*$)|(\\d{3}X.*$)|(\\d{2}XX.*$)|(\\dXXX.*$)|(XXXX.*$)");//pattern to check the start of the sentence matching year
    private final static Pattern onlyYearPattern = Pattern.compile("(\\d{4})|(\\d{3}X)|(\\d{2}XX)|(\\dXXX)");
    private final static Pattern yearMonthPattern = Pattern.compile("\\d{4}\\-\\d{2}");
    private final static Pattern yearMonthDayPattern = Pattern.compile("\\d{4}\\-\\d{2}\\-\\d{2}");
    private final static Pattern yearWeekNumberPattern = Pattern.compile("\\d{4}\\-W\\d{2}");
    private final static Pattern yearSeasonPattern = Pattern.compile("\\d{4}\\-[A-Z]{2}");
    private final static Pattern yearPatternWithX = Pattern.compile("(\\d{3}X)|(\\d{2}XX)|(\\dXXX)");
    public TimelineDate(){    }

    /**
     *
     * @param date a date provided by the StanfordCoreNLP library: it is a normalized entity
     */
    public ArrayList<Date> parse(String date){
        ArrayList<Date> dates = new ArrayList<>();
        //if it starts with YYYY
        //could put year, month and date in for loop as they will need to be reset after each date is created.
        //INTERSECT PX[Y/M/W/D] every X Year/Month/Week/Day: if Y then range is from 0000-9999, if every month then 1 month to 12
        //if XXXX-MM INTERSECT PXY then starting from XXXX-MM (every X years),  or if XXXX-MM INTERSECT PXD then starting from XXXX-MM (every X days)
        // if every week then dates go up , if every day then 1 to 31 (should normalize INTERSECT PX[YMWD] to a text to hold eg
        //if INTERSECT P2Y the normalized text should be every 2 years, and so on.
        //could ignore INTERSECT
        System.out.println("Passing date: "+date);
        if(yearPattern.matcher(date).matches()) {
            String[] splitDate = date.split("/");
            for(String split : splitDate){
                System.out.println(split);
                //check for INTERSECT and remove it appropriately
                if(split.contains("INTERSECT")){
                    System.out.println("Contains INTERSECT");
                    System.out.println("Split gives you length: "+split.split("INTERSECT").length);
                    System.out.println(split.split("INTERSECT")[0]);
                }

                dates.addAll(getDate(split));
            }
            //split into year, month, and day
            //check what format it follows: YYYY, YYYY-MM, YYYY-dd-dd ,YYYY-Wdd, YYYY-Season ,YYYX, YYXX, YXXX
            //for YYYY just set it for the year
            //for YYYY-MM split it and set it for year and month
            //for YYYY-mm-dd split and set it for year, month and day
            //for YYYY-Wdd split it, set the date, and calculate the MM from Wdd
            //for YYYY-Season split it, produce two dates (its a range), set year for both and put start month and day for that season, and for other date put end month and day for that season
            //for YYYX make two dates, one for YYY0 and YYY9
            //for YYXX make two dates, one for YY00 and YY99
            //for YXXX make two dates, one for Y000 and Y999
            //for XXXX-MM range from 0000-MM to 9999-MM
            //every year in month:  XXXX-MM INTERSECT P1Y

            //add dates to list of dates
        }
        //split and call getDate for each part in split
        //there check patterns
        return dates;
    }
    //TODO: case when month is known but not year (could break this into methods to determine year,month and day by passing in split data.
    private ArrayList<Date> getDate(String date){
        ArrayList<Date> dates = new ArrayList<>();
        System.out.print("For: "+date+" have: ");
        if(onlyYearPattern.matcher(date).matches()){//just year
            if(yearPatternWithX.matcher(date).matches()) {//if our date consists unknown year info
                String startYear = date.replaceAll("X", "0");
                String endYear = date.replaceAll("X", "9");
                System.out.print("start year: "+returnDate(startYear,month,day)+" and end year: "+returnDate(endYear,month,day));
                try {
                    dates.add(new SimpleDateFormat("yyyy-MM-dd").parse(returnDate(startYear,month,day)));//make a date object with the data obtained and add it to the list
                } catch (ParseException e) {
                    e.printStackTrace();
                }
                try {
                    dates.add(new SimpleDateFormat("yyyy-MM-dd").parse(returnDate(endYear,month,day)));//make a date object with the data obtained and add it to the list
                } catch (ParseException e) {
                    e.printStackTrace();
                }
            }else{
                //else get date and use default month and day to make date.
                String year = date;
                System.out.print("one year: "+returnDate(year,month,day));
                try {//make a date object with the data obtained and add it to the list
                    dates.add(new SimpleDateFormat("yyyy-MM-dd").parse(returnDate(year,month,day)));
                } catch (ParseException e) {
                    e.printStackTrace();
                }
            }
            //make two dates: startYear-month-day and endYear-month-day and add them to dates
        }else if(yearMonthPattern.matcher(date).matches()){//just year-month
            String[] splitDate = date.split("-");//know it has this format
            String year = splitDate[0];
            String month = splitDate[1];
            System.out.print("year and month: "+returnDate(year,month,day));
            SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd");
            try {//make a date object with the data obtained and add it to the list
                dates.add(simpleDateFormat.parse(returnDate(year,month,day)));
            } catch (ParseException e) {
                e.printStackTrace();
            }
            //make one date: year-month-day and add to dates
        }else if(yearMonthDayPattern.matcher(date).matches()){//full date: year-month-day
            //make one date and add it to dates
            String[] splitDate = date.split("-");//based on format matched
            String year = splitDate[0];
            String month = splitDate[1];
            String day = splitDate[2];
            System.out.print("year, month and day: "+returnDate(year,month,day));
            SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd");
            try {//make a date object with the data obtained and add it to the list
                Date parsedDate  = simpleDateFormat.parse(returnDate(year,month,day));
                dates.add(parsedDate);
            } catch (ParseException e) {
                e.printStackTrace();
            }
        }else if(yearWeekNumberPattern.matcher(date).matches()){//just year-weeknumber
            String[] splitDate = date.split("-");
            String year = splitDate[0];
            String weeknumber = splitDate[1].substring(1);//remove the w in the weeknumber
            //find where the week number points to (ie start year-month-day and end year-month-day)
            SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd");//used to format the date object returned by calendar (can use just date object)
            Calendar calendar = Calendar.getInstance();//use the calendar to calculate the start and end date
            calendar.set(Calendar.YEAR, getInt(year));//set the year to get the appropriate dates for that week
            calendar.set(Calendar.WEEK_OF_YEAR, getInt(weeknumber));//pass in the week number for the range
            calendar.set(Calendar.DAY_OF_WEEK, Calendar.MONDAY);//assuming weeks start on monday
            String start = simpleDateFormat.format(calendar.getTime());
            dates.add(calendar.getTime());//add the start date to the returned list
            calendar.set(Calendar.DAY_OF_WEEK, Calendar.SUNDAY);//and end on sundays
            String end = simpleDateFormat.format(calendar.getTime());
            dates.add(calendar.getTime());//add the tend date to the returned list
            System.out.print("year and week number start: "+start+" end: "+end);//use the calendar to add the date object to the list
        }else if(yearSeasonPattern.matcher(date).matches()){//just year-season
            String[] splitDate = date.split("-");
            String year = splitDate[0];
            String season = splitDate[1];
            Pair<Integer, Integer> startEndMonth = seasonMap.get(season);
            if(startEndMonth != null){//got a pair of start and end month
                //get the start of the season (start of a month), and the end (the start of the next season
                System.out.print("year and season start: "+returnDate(year,startEndMonth.first().toString(),day)+" end: "+returnDate(year,(startEndMonth.second +1)+"",day));
                SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd");
                try {//make a date objects with the data obtained and add them to the list
                    Date parsedStartDate = simpleDateFormat.parse(returnDate(year,startEndMonth.first().toString(),day));
                    Date parsedEndDate = simpleDateFormat.parse(returnDate(year,(startEndMonth.second +1)+"",day));
                    dates.add(parsedStartDate);
                    dates.add(parsedEndDate);
                } catch (ParseException e) {
                    e.printStackTrace();
                }
            }


            //need to find what start month-day and end month-day this season corresponds to
            //make two dates: with start year-month-day and end year-month-day (year might be increased if season goes to next year
            //check if end month is < start month, now need to increase year by 1
            /*
            Winter December , January, February (12,1,2) (0-2)
            Spring is March, April and May (3,4,5) (3-5)
            Summer is June, July, August (6,7,8) (6-8)
            Autumn September, October, November (9,10,11) (9-11)
             */
            //according to educationuk.org:
        }//else if you have [DATE] INTERSECT PX[YMDW] then split into: [DATE] and INTERSECT PX[YMDW]
        //run just the [DATE] through this method again, interpret intersect to get string eg: INTERSECT P1Y every year
        //add the date to dates and pass the string back to the Result object.
        System.out.println("");
        return dates;
    }

    /**
     * enforces rule that arraylist can only have two dates
     */
    private void cleanUp(){

    }

    private String returnDate(String year, String month, String day){
        return String.format("%s-%s-%s",year,month,day);
    }

    private int getInt(String number){
        try{
            return new Integer(number).intValue();
        }catch (Exception e){
            return 1;
        }
    }
}
