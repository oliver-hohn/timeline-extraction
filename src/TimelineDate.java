import java.util.regex.Pattern;

/**
 * Represents the dates for a timeline event.
 */
public class TimelineDate {
    private String year;
    private String month;
    private String day;

    private final static Pattern yearPattern = Pattern.compile("(\\d{4}.*$)|(\\d{3}X.*$)|(\\d{2}XX.*$)|(\\dXXX.*$)");//pattern to check the start of the sentence matching year

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
        if(yearPattern.matcher(date).matches()) {
            System.out.println("Date is legal: "+date);
            //split to check for range
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
            //add dates to list of dates
        }
    }
}
