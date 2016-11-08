import java.util.ArrayList;

/**
 * Created by Oliver on 29/10/2016.
 */
public class Result {
    private String date;
    private String event;
    private String subject;

    public Result(){
        this("","","");
    }

    public Result(String date, String event, String subject){
        this.date = date;
        this.event = event;
        this.subject = subject;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public String getEvent() {
        return event;
    }

    public void setEvent(String event) {
        this.event = event;
    }

    public String getSubject() {
        return subject;
    }

    public void setSubject(String subject) {
        this.subject = subject;
    }

    public void addToDate(String addDate){
        date += " "+addDate;
    }

    public void addToSubject(String subject){
        if(this.subject.equals("")) {
            System.out.println("yo");
            setSubject(subject);
            return;
        }
        this.subject += "-"+subject;
    }

    public void addToEvent(ArrayList<String> strings){
        for(String s: strings){
            event += s+"\n";
        }
    }

    @Override
    public String toString() {
        return String.format("Date: %s, Subject: %s, Event: %s",date,subject,event);
    }
}
