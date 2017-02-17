package backend.process;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashSet;
import java.util.Set;

/**
 * Holds the data for one event in the Timeline.
 */
public class Result implements Comparable<Result>, Cloneable {
    private ArrayList<String> dates;
    private String event;
    private Set<String> subjects;
    private TimelineDate timelineDate;
    private FileData fileData;
    private String originalString;

    /**
     * Initialises variables.
     */
    public Result() {
        this("");
    }

    /**
     * Initialises variables and sets the event.
     *
     * @param event the event text of this event.
     */
    private Result(String event) {
        dates = new ArrayList<>();
        this.event = event;
        subjects = new HashSet<>();
        timelineDate = new TimelineDate();
        originalString = "";
    }

    /**
     * Checks whether this backend.process.Result already has a given date (in String format)
     *
     * @param date the String date being checked.
     * @return true if the list of String dates has date, false otherwise.
     */
    public boolean hasDate(String date) {
        for (String dDate : dates) {
            if (dDate.equals(date)) {
                return true;
            }
        }
        return false;
    }

    /**
     * The event string of this event.
     *
     * @return the string event of this event.
     */
    public String getEvent() {
        return event;
    }

    /**
     * Sets the event of this Timeline event.
     *
     * @param event what we are setting as an event.
     */
    public void setEvent(String event) {
        this.event = event;
    }

    /**
     * Add a string date to the list of held string dates, dates relevant to this event.
     *
     * @param date the String date that is being added.
     */
    public void addDate(String date) {
        dates.add(date);
    }

    /**
     * Get the list of String dates held.
     *
     * @return the list of String dates held.
     */
    public ArrayList<String> getDates() {
        return dates;
    }

    /**
     * Get the list of Subjects relevant to this event.
     *
     * @return the list of Subjects held.
     */
    public Set<String> getSubjects() {
        return subjects;
    }

    /**
     * Add a subject to the list of Subjects held.
     *
     * @param subject a subject to add.
     */
    public void addSubject(String subject) {
        subjects.add(subject);
    }

    /**
     * @return a String showing all the information held by this backend.process.Result object.
     */
    @Override
    public String toString() {
        return String.format("Date: %s, Subject: %s, Event: %s", timelineDate, subjects, event);
    }

    /**
     * For the given passed in String date, it will update the backend.process.TimelineDate of this backend.process.Result object.
     *
     * @param date a String that contains date information that needs to be passed into the timeline date of this backend.process.Result object.
     */
    public void addDate_1(String date, String baseDate) {
        System.out.println("About to parse: " + date);
        timelineDate.parse(date, baseDate);
    }


    /**
     * Used to compare two backend.process.Result objects by their backend.process.TimelineDate.
     *
     * @param o the other backend.process.Result object.
     * @return the result of comparing the backend.process.TimelineDate of this object, with the backend.process.TimelineDate of the other (input) backend.process.Result object.
     */
    @Override
    public int compareTo(Result o) {
        return this.timelineDate.compareTo(o.timelineDate);
    }

    /**
     * Get the TimelineDate for this Result.
     *
     * @return the date data used for this Result in the Timeline.
     */
    public TimelineDate getTimelineDate() {
        return timelineDate;
    }

    /**
     * Set the TimelineDate for this Result.
     *
     * @param timelineDate the date data used for this Result in the Timeline.
     */
    public void setTimelineDate(TimelineDate timelineDate) {
        this.timelineDate = timelineDate;
    }

    /**
     * Get the FileData for this Result.
     *
     * @return the File data of the File that produced this Result.
     */
    public FileData getFileData() {
        return fileData;
    }

    /**
     * Set the FileData for this Result.
     *
     * @param fileData the File data of the File that produced this Result.
     */
    public void setFileData(FileData fileData) {
        this.fileData = fileData;
    }

    /**
     * Used to produce a String of the Subjects of this Result. The String is every item in the Subjects set, separated
     * by commas.
     *
     * @return a String of the Subjects of this Result, separated by commas.
     */
    public String getSubjectsAsString() {
        String toReturn = "";
        for (int i = 0; i < subjects.size(); i++) {
            toReturn += subjects.toArray()[i];
            if (i < subjects.size() - 1) {
                toReturn += ", ";
            }
        }
        return toReturn;
    }

    /**
     * Makes a new Result object, with the same data as this one but not referencing to the same place in memory. So
     * changes in the new Result object do not change the data of this Result object.
     *
     * @return a new Result object with the same data but not pointing to the data in memory.
     * @throws CloneNotSupportedException
     */
    @Override
    public Object clone() throws CloneNotSupportedException {
        super.clone();
        Result copyResult = new Result();
        //copying the dates
        TimelineDate copyTimelineDate = new TimelineDate();
        copyTimelineDate.setDate1((Date) timelineDate.getDate1().clone());
        if (timelineDate.getDate2() != null) {
            copyTimelineDate.setDate2((Date) timelineDate.getDate2().clone());
        } else {
            copyTimelineDate.setDate2(null);
        }
        copyResult.setTimelineDate(copyTimelineDate);
        //copying the event
        copyResult.setEvent(event);
        //copying the subjects
        copyResult.subjects = new HashSet<>(subjects);
        //set the filedata
        copyResult.setFileData(fileData);//all results of the same file point to the same filedata (not a unique one)
        //set the original sentence
        copyResult.setOriginalString(originalString);
        return copyResult;
    }

    @Override
    public boolean equals(Object obj) {
        if(obj instanceof Result){
            Result other = (Result) obj;
            return subjects.equals(other.getSubjects()) && event.equals(other.event) && originalString.equals(other.originalString)
                    && fileData.equals(other.fileData) && timelineDate.equals(other.getTimelineDate());
        }
        return false;
    }

    /**
     * Get the original sentence that produced this Result object.
     *
     * @return the original sentence that produced this.
     */
    public String getOriginalString() {
        return originalString;
    }

    /**
     * Set the original sentence that produced this Result object.
     *
     * @param originalString the original sentence that produced this.
     */
    public void setOriginalString(String originalString) {
        this.originalString = originalString;
    }
}
