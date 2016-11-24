import java.util.ArrayList;

/**
 * Created by Oliver on 29/10/2016.
 */
public class Result {
    private ArrayList<String> dates;
    private String event;
    private ArrayList<String> subjects;//could be a set to avoid duplicates

    public Result(){
        this("","");
    }

    public Result(String date, String event){
        dates = new ArrayList<>();
        this.event = event;
        subjects = new ArrayList<>();
    }

    public boolean hasDate(String date){
        for(String dDate: dates){
            if(dDate.equals(date)){
                return true;
            }
        }
        return false;
    }

    public String getEvent() {
        return event;
    }

    public void setEvent(String event) {
        this.event = event;
    }

    public void addDate(String date){
        dates.add(date);
    }

    public ArrayList<String> getDates(){
        return dates;
    }
    public ArrayList<String> getSubjects(){
        return subjects;
    }

    public void addSubject(String subject){
        subjects.add(subject);
    }
    @Override
    public String toString() {
        return String.format("Date: %s, Subject: %s, Event: %s",dates, subjects,event);
    }
}
