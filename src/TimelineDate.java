import edu.stanford.nlp.util.Index;
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
    private static final Map<String, Pair<String,String>> seasonMap;
    static {
        Map<String, Pair<String,String>> map = new HashMap<>();
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

    private Calendar calendar;
    public TimelineDate(){
        calendar = Calendar.getInstance();
    }

    /**
     *
     * @param date a date provided by the StanfordCoreNLP library: it is a normalized entity
     */
    public ArrayList<Date> parse(String date){
        ArrayList<Date> dates = new ArrayList<>();
        //splitting INTERSECT
        String[] splitDate = date.split("INTERSECT");
        if(splitDate.length > 0){// on the first part of the date, which is just a date, get its specfic date
            String possibleDates = splitDate[0];//this date could also be a range, ie include /
            String[] splitRange = possibleDates.split("/");
            for(String possibleDate: splitRange){
                dates.addAll(getDate(possibleDate));// from processing the individual date, add it to dates
            }
        }
        //if we had INTERSECT then we should process it for additional info to show
        //if we have more than 2 dates in the list, then keep the minimum date and max date and remove all the others
        return dates;
    }

    private ArrayList<Date> getDate(String date){
        ArrayList<Date> dates = new ArrayList<>();
        String year1 = year;//setting the default values
        String month1 = month;
        String day1 = day;
        String year2 = null;
        String month2 = null;
        String day2 = null;

        //now need to split date into its individual components
        String[] dateInfo = date.split("-");
        for(int i=0; i<dateInfo.length; i++){
            if(i == 0){//this can only be a year
                //check year format
                if(onlyYearPattern.matcher(dateInfo[i]).matches()){
                    year1 = dateInfo[i].replace("X","0");
                    if(dateInfo[i].contains("X")) {//if we do have a range then we need to set the values for the second date
                        year2 = dateInfo[i].replace("X","9");
                        month2 = "12";//last day of the second year
                        day2 = "31";//assuming a range for 1980s means 1980 to the last day of 1989 (maximum possible range)
                    }
                }
                continue;//move onto the next part of the year
            }else if(i == 1){//this can be a week number, a month number or a season
                //checking if its a month
                System.out.println("Checking: "+dateInfo[i]);
                if(onlyMonthPattern.matcher(dateInfo[i]).matches()){
                    System.out.println("In onlyMonthPattern");
                    month1 = dateInfo[i];
                }else if(onlyWeekNumberPattern.matcher(dateInfo[i]).matches()){//checking if its a week number
                    //calculate month and start day-end
                    //split W from actual week number
                    String weekNumber = dateInfo[i].substring(1);//W is the first part of the string, after it is the week number
                    calendar.set(Calendar.YEAR, getInt(year1));//should be set from previously
                    calendar.set(Calendar.WEEK_OF_YEAR, getInt(weekNumber));
                    calendar.set(Calendar.DAY_OF_WEEK, Calendar.MONDAY);//assuming we start on monday and end on sunday
                    month1 = new SimpleDateFormat("MM").format(calendar.getTime());
                    day1 = new SimpleDateFormat("dd").format(calendar.getTime());
                    //now for the end of the week
                    calendar.set(Calendar.DAY_OF_WEEK, Calendar.SUNDAY);
                    month2 = new SimpleDateFormat("MM").format(calendar.getTime());
                    day2 = new SimpleDateFormat("dd").format(calendar.getTime());
                    year2 = new SimpleDateFormat("yyyy").format(calendar.getTime());
                }else if(onlySeasonPattern.matcher(dateInfo[i]).matches()){//checking if its a season
                    String season = dateInfo[i];
                    Pair<String, String> seasonPair = seasonMap.get(season);//get the start and end month for the season
                    if(seasonPair != null){//year1 should be set previously
                        month1 = seasonPair.first.toString();
                        month2 = seasonPair.second.toString();
                        day2 = "31";//set the second day as we are using a range
                        if(seasonPair.first.equals("12")){//move onto the next year, so year2 should be updated
                            year2 = (getInt(year1)+1)+"";
                        }else{//we dont need to increment the year as the season is within the same year
                            year2 = year1;
                        }
                    }
                }
                continue;//move onto next part
            }else if(i == 2){//can only be a day
                if(onlyDayPattern.matcher(dateInfo[i]).matches()){//got the day
                    day1= dateInfo[i];
                }
            }
        }
        System.out.println("For: "+date);
        System.out.println("For Date1 we have: "+year1+"-"+month1+"-"+day1);
        System.out.println("For Date2 we have: "+year2+"-"+month2+"-"+day2);
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
        }catch (ParseException e){
            //could not add the dates
        }
        System.out.println("\n");
        return dates;
        /* according to educationuk.org:
            Autumn September, October, November (9,10,11) (9-11)
            Winter December , January, February (12,1,2) (0-2)
            Spring is March, April and May (3,4,5) (3-5)
            Summer is June, July, August (6,7,8) (6-8)
         */
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
