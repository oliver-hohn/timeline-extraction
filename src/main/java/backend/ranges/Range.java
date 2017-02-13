package backend.ranges;

import backend.process.Result;
import backend.process.TimelineDate;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * A Range of Dates (which can be only one date) which can be represented as a Node in a Tree, due to it holding a list of
 * its sub-ranges.
 */
public class Range implements Cloneable {
    private List<Result> results;//results that are exactly in this range
    private Date date1;//first part of range
    private Date date2;//only if we have a range of size > 0
    private List<Range> children;//the children in this range within constraint children.date1 >= date1 children.date1 < date2 && children.date2 <= date2

    public Range(Date date1, Date date2) {
        this.date1 = date1;
        this.date2 = date2;
        results = new ArrayList<>();
        children = new ArrayList<>();
    }

    /**
     * For the given Result, attempt to add it to the Results currently held by this Range, if that is not possible
     * check whether it is within the bounds of dates, if it isn't then return false (as we can't add it to this Range),
     * else attempt to add it to the children (and their children and so on).
     *
     * @param result the given Result.
     * @return true if the Result was added to this Range (i.e. this Tree) or its children or its children-children etc;
     * false otherwise.
     */
    public synchronized boolean add(Result result) {
        TimelineDate timelineDate = result.getTimelineDate();
        //check whether the given result can be added to this exact range (i.e it holds a timelinedate with a range of
        //dates exactly equal to this). Checks of null, ie if we have null then the other must have it, else its not part of this exact range
        System.out.println("Attemping to add: "+timelineDate+" to :"+printDate(date1)+" -> "+printDate(date2));
        System.out.println("Its children are: ");
        for(Range c:children){
            System.out.println(c);
        }
        if (timelineDate.getDate1().equals(date1) && ((date2 == null && timelineDate.getDate2() == null)
                || (date2 != null && timelineDate.getDate2() != null && date2.equals(timelineDate.getDate2())))) {
            //if we have the same start date and date2 != null -> date2 are equals
            //then this Result belongs to the results of this Range
            System.out.println("For the given Result: "+result+"its range is exactly in: "+printDate(date1)+" -> "+printDate(date2));
            results.add(result);
            return true;//we added it
        }
        //we couldnt add it directly to this Range, try its children
        //TODO: check we are within the constraints, before attempting to add to the children, to improve performance
        System.out.println("Adding result to children");
        return addResultToChildren(result);
    }

    /**
     * For the given result, attempt to add it to the Children of this Range (which recursively calls add on their
     * children until we reach a range exactly equal to it, or we have to create our own range because there is none).
     *
     * @param result the given Result.
     * @return true if the Result was added to the Children of this Range, false otherwise.
     */
    private boolean addResultToChildren(Result result) {
        //for all of the childrens, attempt to add this Result
        for (Range child : children) {
            System.out.println("Child: "+child);
            if (child.add(result)) {//see if we can add it to one of the children (ie holds the same range, or we reach all
                //the leaf nodes because we couldnt add it so we make a new child (as this is where the range
                //should be
                return true;//we could add it to one of the children
            }
        }
        System.out.println("Couldnt add to children");
        //we couldn't add it to any of the childrens so make new child and try to add it
        TimelineDate timelineDate = result.getTimelineDate();
        Range newChild = new Range(timelineDate.getDate1(), timelineDate.getDate2());
        newChild.add(result);//the new potential range child holds its result
        System.out.println("Calling addChild");
        return addChild(newChild);// try and add this child (need to check constraints
    }


    /**
     * For the given Range (r') attempt to add it to this Range (r). For this to occur it must be within the range of dates of
     * this Range (i.e. r'.date1 >= r.date1 && r'.date1 <r.date2 && r'.date2 < r.date2) or it must overlap (i.e. date 1
     * is within the range, but date2 isn't or vice versa). In the latter case we extend this Range, such that it holds
     * the old range and the given range.
     *
     * @param potentialChild the given Range.
     * @return true if the potentialChild could be added to the children of this Range, or we could extend the Range;
     * false otherwise.
     */
    public boolean addChild(Range potentialChild) {
        /*
            if you are within the constraints
                then attempt to add it to the children,if it cant because there are no
                    children, or it doesnt fit the childrens constraints then add it to this Range's children
            check for partial constraints
                if it matches then extend the ranges and add it to the new extended range (that holds as a child the old Range of this)
            otherwise return false (it doesn't belong in this area)
         */
        System.out.println("Now checking cases for: "+printDate(potentialChild.date1)+" and "+printDate(potentialChild.date2)
                +" In: "+printDate(date1)+ " and "+printDate(date2));
        if (isWithinConstraints(date1, date2, potentialChild.date1)
                && (potentialChild.date2 == null || isWithinConstraints(date1, date2, potentialChild.date2))) {
            System.out.println("Within constraints: " + printDate(potentialChild.date1) + " and " + printDate(potentialChild.date2));
            //we are within the constraints, try to add to children, if cant because there are none, or we cant then add directly
            for (Range child : children) {
                System.out.println("Trying to add to child: " + child);
                if (child.addChild(potentialChild)) {//successfully added to one of them
                    System.out.println("We added");
                    return true;
                }
            }
            //we couldnt add to any of them, then add directly to my children
            System.out.println("Adding potential child");
            children.add(potentialChild);
            System.out.println("Added potential child");
            return true;
        } else if (isWithinConstraints(date1, date2, potentialChild.date1) && (potentialChild.date2 != null && !isWithinConstraints(date1, date2, potentialChild.date2))) {
            System.out.println("Within constraints: " + printDate(potentialChild.date1) + " but not " + printDate(potentialChild.date2));
            //partial constraints
            //date1 is in the range but date2 isnt so extend and us to the extensions children as we didnt fit in previous range (which is now its child)
            extendRange(date1, potentialChild.date2);
            children.add(potentialChild);
            return true;//if we called addChild it would just loop on the child that was the previous range, and thats the only child so there is no point checking just add to this
        } else if (!isWithinConstraints(date1, date2, potentialChild.date1) && (potentialChild.date2 != null && isWithinConstraints(date1, date2, potentialChild.date2))) {
            System.out.println("not Within constraints: " + printDate(potentialChild.date1) + " but yes " + printDate(potentialChild.date2));
            //date2 is in the range but date1 isnt so extend
            extendRange(potentialChild.date1, date2);
            children.add(potentialChild);
            return true;
        }
        System.out.println("We didnt add");
        return false;
    }

    /**
     * Used to extend the current Range to the given Dates, and hold the previous data in a new Range as a child.
     *
     * @param date1 the start of the extended Range.
     * @param date2 the end of the extended Range.
     */
    private void extendRange(Date date1, Date date2) {
        try {
            System.out.println("Extending the range to: " + printDate(date1) + " and " + printDate(date2));
            System.out.println("The Range originally is: "+printDate(this.date1)+" and "+printDate(this.date2));
            Range cloneRange = (Range) clone();//the date of this will be a new child, we are setting its parent
            //clear all the values and set the new ones
            clear();
            this.date1 = date1;//extended range value
            this.date2 = date2;//extended range value
            children.add(cloneRange);//add what used to be this (but now we cloned it) as out child
        } catch (CloneNotSupportedException e) {
            e.printStackTrace();
        }
    }

    /**
     * For the given Date, return a String of the format: dd-MM-yyyy G (days-months-years ERA)
     *
     * @param date the given Date.
     * @return a String of the format dd-MM-yyyy G or null if the given date can't be formatted (i.e. is null)
     */
    private String printDate(Date date) {
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("dd-MM-yyyy G");
        try {
            return simpleDateFormat.format(date);
        } catch (Exception e) {
        }
        return null;
    }

    /**
     * Clear all the data held by the current Range.
     */
    private void clear() {
        date1 = null;
        date2 = null;
        children = new ArrayList<>();
        results = new ArrayList<>();
    }

    /**
     * Check that the given Date toCheck is within the other two given Dates constraint1 and constraint2 (both inclusive)
     *
     * @param constraint1 the given start constraint Date (inclusive).
     * @param constraint2 the given end constraint Date (inclusive).
     * @param toCheck     the given Date we are checking.
     * @return true if the given Date to check is within the two constraints, or if any of them are null equal to the other;
     * false otherwise.
     */
    private boolean isWithinConstraints(Date constraint1, Date constraint2, Date toCheck) {
/*        System.out.println("Date1 constraint: " + printDate(constraint1) + " Date2 constraint: " + printDate(constraint2)
                + "We are checking: " + printDate(toCheck));*/
        if (constraint1 != null && constraint2 != null) {
            return toCheck.compareTo(constraint1) >= 0 && toCheck.compareTo(constraint2) <= 0;
        }
        if (constraint1 == null && constraint2 != null) {
            return toCheck.equals(constraint2);
        }
        if (constraint1 != null) {
            return toCheck.equals(constraint1);
        }
        return false;
    }

    /**
     * Return a clone of this Range. Holds the same data.
     *
     * @return an Object that can be cast to a Range object, which holds the current Data of the Range that called it
     * (when clone() was called).
     * @throws CloneNotSupportedException
     */
    @Override
    protected Object clone() throws CloneNotSupportedException {
        super.clone();
        Range cloneRange = new Range(date1, date2);
        cloneRange.children = new ArrayList<>(children);
        cloneRange.results = new ArrayList<>(results);
        return cloneRange;
    }

    /**
     * Returns a String of the given Range.
     *
     * @return a String that shows the range of Dates held by this Range and the its child Ranges (recursive).
     */
    @Override
    public String toString() {
        String toReturn = printDate(date1);
        if (date2 != null) {
            toReturn += " -> " + printDate(date2);
        }
        toReturn += " has children: " + children;
        return toReturn;
    }
}
