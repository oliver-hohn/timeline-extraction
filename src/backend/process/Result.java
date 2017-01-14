package backend.process;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;

/**
 * Holds the data for one event in the Timeline.
 */
public class Result implements Comparable<Result> {
    private ArrayList<String> dates;
    private String event;
    private Set<String> subjects;
    private TimelineDate timelineDate;
    private FileData fileData;

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
        return o.timelineDate.compareTo(this.timelineDate);
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
}
