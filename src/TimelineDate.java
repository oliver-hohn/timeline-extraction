import java.util.regex.Pattern;

/**
 * Represents the dates for a timeline event.
 */
public class TimelineDate {
    private String year;
    private String month;
    private String day;
                                                                    //wont pick up for dates like: every year in january, which has format XXXX-01
    private final static Pattern yearPattern = Pattern.compile("(\\d{4}.*$)|(\\d{3}X.*$)|(\\d{2}XX.*$)|(\\dXXX.*$)");//pattern to check the start of the sentence matching year
    private final static Pattern onlyYearPattern = Pattern.compile("(\\d{4})|(\\d{3}X)|(\\d{2}XX)|(\\dXXX)");
    private final static Pattern yearMonthPattern = Pattern.compile("\\d{4}\\-\\d{2}");
    private final static Pattern yearMonthDayPattern = Pattern.compile("\\d{4}\\-\\d{2}\\-\\d{2}");
    private final static Pattern yearWeekNumberPattern = Pattern.compile("\\d{4}\\-W\\d{2}");
    private final static Pattern yearSeasonPattern = Pattern.compile("\\d{4}\\-[A-Z]{2}");
    private final static Pattern yearPatternWithX = Pattern.compile("(\\d{3}X)|(\\d{2}XX)|(\\dXXX)");
    public TimelineDate(){
        year = "0001";
        month = "01";
        day = "01";
    }

    /**
     *
     * @param date a date provided by the StanfordCoreNLP library: it is a normalized entity
     */
    public void parse(String date){
        //if it starts with YYYY
        System.out.println("Passing date: "+date);
        if(yearPattern.matcher(date).matches()) {
            //System.out.println("Date is legal: "+date);
            //split to check for range
            //System.out.println(date.split("/").length);
            String[] splitDate = date.split("/");
            for(int i=0; i<splitDate.length; i++){
                //split into year month and day
                if(onlyYearPattern.matcher(splitDate[i]).matches()){//checking just for year dates
                    System.out.println("Date: "+splitDate[i]+ " is just a year YYYY");
                    if(yearPatternWithX.matcher(splitDate[i]).matches()) {
                        //need to check X's (check if it has any), for first, replace every X with 0, for last replace every X with 9
                        String startYear = splitDate[i].replaceAll("X", "0");
                        String endYear = splitDate[i].replaceAll("X", "9");

                        System.out.println("Our years for: "+splitDate[i]+" is: "+startYear+" - "+endYear);
                    }
                    //break into year and set it
                }else if(yearMonthPattern.matcher(splitDate[i]).matches()){//checking just for year-month dates
                    System.out.println("Date: "+splitDate[i]+" has the format YYYY-MM");
                    //break into year and month and set it
                    String[] splitString = splitDate[i].split("-");//as we are in if, the string has format YYYY-MM can split it
                    String year = splitString[0];
                    String month = splitString[1];

                    System.out.println("For: "+splitDate[i]+" we have as year: "+year+" and month: "+month);
                }else if(yearMonthDayPattern.matcher(splitDate[i]).matches()){//checking for year-month-date
                    System.out.println("Date: "+splitDate[i]+" has the format YYYY-MM-DD");
                    //break into year, month and day, or just set it to date directly
                    String[] splitString = splitDate[i].split("-");//because of above matching we know we have a string of the format YYYY-MM-DD
                    String year = splitString[0];
                    String month = splitString[1];
                    String day = splitString[2];

                    System.out.println("For: "+splitDate[i]+" we have as year: "+year+" , month: "+month+" and day: "+day);
                }else if(yearWeekNumberPattern.matcher(splitDate[i]).matches()){//checking for year-weeknumber
                    System.out.println("Date: "+splitDate[i]+" has the format YYYY-Wdd");
                    //break into year and weeknumber, then calculate month from week number
                    String[] splitString = splitDate[i].split("-");
                    String year = splitString[0];
                    String weekNo = splitString[1].substring(1);//need to remove the W to just get the week number, gives you substring from position 1

                    System.out.println("For: "+splitDate[i]+" we have as year: "+year+" and week number: "+weekNo);
                }else if(yearSeasonPattern.matcher(splitDate[i]).matches()){//checking for year-season
                    System.out.println("Date: "+splitDate[i]+" has the format YYYY-Season");
                    //break into year and season, then create two date objects, with same year, one with start season month and day and other with end season month and day
                    String[] splitString = splitDate[i].split("-");
                    String year = splitString[0];
                    String season = splitString[1];

                    System.out.println("For: "+splitDate[i]+" we have as year: "+year+" and season: "+season);
                }
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
    }
}
