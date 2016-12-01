import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;

/**
 * Created by Oliver on 29/10/2016.
 */
public class Result {
    private ArrayList<String> dates;
    private String event;
    private Set<String> subjects;

    public Result(){
        this("","");
    }

    public Result(String date, String event){
        dates = new ArrayList<>();
        this.event = event;
        subjects = new HashSet<>();
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
    public Set<String> getSubjects(){
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
