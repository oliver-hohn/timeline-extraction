package backend.ranges;

import backend.process.Result;
import backend.process.TimelineDate;

import java.text.SimpleDateFormat;
import java.util.*;

/**
 * A Range of Dates (which can be only one date) which can be represented as a Node in a Tree, due to it holding a list of
 * its sub-ranges.
 */
public class Range implements Cloneable, Comparable<Range> {
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
     * For the given Result, check that the dates of the Result are partially in the scope of this Range, if that is not
     * possible then return false.
     * If the Result is within this Rage, then attempt to find between the current Range and its children a Range where
     * the Results range of dates fit in perfectly, if it can be added then add it else attempt to create a new Range
     * child of this Range and find its location to place it.
     * <p>
     * Algorithm given by:
     * 1.check constraints, if they are false then return false else move onto step 2.
     * 2.attempt to add the Result to one of the children (or children-children, or etc) of this Range where they both
     * have the same Range; move onto step 3 if that is unsuccessful.
     * 3.create a new Range and find the location of where to add it in the Range (extending the Range where necessary)
     *
     * @param result the given Result.
     * @return true if the Result was added to this Range (i.e. this Tree) or its children or its children-children etc;
     * false otherwise.
     */
    public boolean add(Result result) {
        TimelineDate timelineDate = result.getTimelineDate();
        //check constraints
        if (!shouldAdd(timelineDate)) {//check constraints if we can even add to this range (ie are we in the root constraints)
            return false;
        }
        //attempt to add through an existing range
        Range toAdd = checkCanAdd(result);
        if (toAdd != null) {
            toAdd.results.add(result);//add to the results of the given range where we could add
            return true;
        }
        //now we try to extend the range
        return createRangeAndAdd(result);
    }

    /**
     * Whether or not the given TimelineDate is partially or fully in the range of Dates of this Range. Whether or not
     * we should try to add the Result of this TimelineDate to this Range.
     *
     * @param timelineDate the given TimelineDate.
     * @return true if the TimelineDate is within/partially (ie if date1 is in the Range but not date2, or vice versa,
     * or both dates are within the range)in this Range; false otherwise.
     */
    private boolean shouldAdd(TimelineDate timelineDate) {
        //check the date1 is in constraint to t.date1 then if date2 then check with date2
        return isWithinConstraints(date1, date2, timelineDate.getDate1())
                || (timelineDate.getDate2() != null && isWithinConstraints(date1, date2, timelineDate.getDate2()));
    }

    /**
     * Attempt to find a Range (either this or one of its children, or its children-children, etc) that has the date
     * values that match the given Result.
     *
     * @param result the given Result.
     * @return a Range that holds the same exact date values as the TimelineDate of the Result or null if that is not
     * possible.
     */
    private Range checkCanAdd(Result result) {
        TimelineDate timelineDate = result.getTimelineDate();
        if (timelineDate.getDate1().equals(date1) && ((date2 == null && timelineDate.getDate2() == null)
                || (date2 != null && timelineDate.getDate2() != null && date2.equals(timelineDate.getDate2())))) {
            return this;
        }
        Range toReturn = null;
        for (Range child : children) {
            toReturn = child.checkCanAdd(result);
            if (toReturn != null) {
                return toReturn;
            }
        }
        return toReturn;
    }

    /**
     * For the given Result, create a Range using its TimelineDate and attempt to find the location where this Range
     * should be added in the Tree structure (ie find the lowest/best possible Range that should contain the created
     * Range).
     * Normally called if we couldn't find any Range that has the same exact Dates so we create a Range to contain it
     * and attempt to find its spot (extending Ranges where necessary to self contain Ranges in bigger Ranges).
     *
     * @param result the given Result.
     * @return true if the created Range was added to this (sub-)tree, false otherwise.
     */
    private boolean createRangeAndAdd(Result result) {
        //we couldn't add it to any of the childrens so make new child and try to add it
        TimelineDate timelineDate = result.getTimelineDate();
        Range newChild = new Range(timelineDate.getDate1(), timelineDate.getDate2());
        newChild.add(result);//the new potential range child holds its result
        return addChild(newChild);// try and add this child (need to check constraints
    }


    /**
     * For the given Range (r') attempt to add it to this Range (r). For this to occur it must be within the range of dates of
     * this Range (i.e. r'.date1 >= r.date1 && r'.date1 <r.date2 && r'.date2 < r.date2) or it must overlap (i.e. date 1
     * is within the range, but date2 isn't or vice versa). In the latter case we extend this Range, such that it holds
     * the old range and the given range.
     *
     * @param potentialChild the given Range (r').
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
        if (isWithinConstraints(date1, date2, potentialChild.date1)
                && (potentialChild.date2 == null || isWithinConstraints(date1, date2, potentialChild.date2))) {
            //we are within the constraints, try to add to children Ranges, if cant because there are none, or we cant then add directly to this Ranges children
            for (Range child : children) {
                if (child.addChild(potentialChild)) {//successfully added to one of them
                    return true;
                }
            }
            //we couldnt add to any of them, then add directly to my children
            children.add(potentialChild);
            return true;
            //partial constraints
        } else if (isWithinConstraints(date1, date2, potentialChild.date1) && (potentialChild.date2 != null && !isWithinConstraints(date1, date2, potentialChild.date2))) {
            //checking if we need to extend the range (this case the first date is within the constraints but date2 isnt
            //date1 is in the range but date2 isnt so extend and us to the extensions children as we didnt fit in previous range (which is now its child)
            extendRange(date1, potentialChild.date2);//extend the range and add the child to the now extended range (as we fit in the now extended
            children.add(potentialChild);//range, but not in its other child which was the previous range held before extending, else we wouldnt be here)
            return true;//if we called addChild it would just loop on the child that was the previous range, and thats the only child so there is no point checking just add to this
        } else if (!isWithinConstraints(date1, date2, potentialChild.date1) && (potentialChild.date2 != null && isWithinConstraints(date1, date2, potentialChild.date2))) {
            //date2 is in the range but date1 isnt so extend and add to the children of the extended range.
            extendRange(potentialChild.date1, date2);
            children.add(potentialChild);
            return true;
        }
        //We werent able to add the potentialRange to this Range,
        return false;//so return false (this method is recursive, so it will try on the next child of the parent Range that called it on this child)
    }

    /**
     * Used to extend the current Range to the given Dates, and hold the previous data in a new Range as a child.
     *
     * @param date1 the start of the extended Range.
     * @param date2 the end of the extended Range.
     */
    private void extendRange(Date date1, Date date2) {
        try {
            Range cloneRange = (Range) clone();//the date of this will be a new child, we are setting its parent
            clear();//clear all the values and set the new ones
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
     * @return a String that shows the range of Dates held by this Range, its Result and the its child Ranges (recursive).
     */
    @Override
    public String toString() {
        String toReturn = printDate(date1);
        if (date2 != null) {
            toReturn += " -> " + printDate(date2);
        }
        toReturn += " has results: " + results;
        toReturn += " and has children: \n" + children;
        return toReturn;
    }

    /**
     * Overwrote the equals method to check whether two Ranges are equal to each other: that is they hold the same Dates,
     * results and their children are equal (recursive).
     *
     * @param obj the other Range we are checking if its equal to this Range.
     * @return true if the other Range has the same Dates as this Range, holds the same Results as this Range, and their
     * children are equal.
     */
    @Override
    public boolean equals(Object obj) {
        try {
            Range other = (Range) obj;
            if (date1.equals(other.date1) && ((date2 == null & other.date2 == null) || (date2 != null & other.date2 != null && date2.equals(other.date2)))) {
                if (children.isEmpty() && other.children.isEmpty()) {
                    return results.equals(other.results);
                } else {//they have children
                    return results.equals(other.results) && children.equals(other.children);
                }
            }
        } catch (Exception e) {
            return false;
        }
        return false;
    }


    /**
     * Sort the children held by this Range, and calls sortChildren on its children.
     * Sorts them such that the resulting children list is in descending order based on date1 (ie at index 0 you have
     * the newest range and at the last index the range with date1 furthest away.
     */
    public void sortChildren() {
        if (children.size() > 0) {
            Collections.sort(children);
            for (Range range : children) {
                range.sortChildren();
            }
        }
    }


    /**
     * Implemented the Comparable interface to sort Ranges.
     *
     * @param o the other Range we are comparing to.
     * @return 1 if the date1 of this Range is greater than the other, 0 if they are equal and -1 if this Range's date1
     * is less than the other.
     */
    @Override
    public int compareTo(Range o) {
        if (o.date1 != null && this.date1 != null) {
            return date1.compareTo(o.date1);
        }
        if (this.date1 == null) {
            return -1;
        }
        return 1;
    }

    /**
     * Get the list of Range children held by this Range.
     *
     * @return the list of Range children.
     */
    public List<Range> getChildren() {
        return children;
    }

    /**
     * Get a String representation of date1 and date2 (if set) of this Range (i.e. a String of the range of dates held
     * by this Range).
     *
     * @return a String representation of the range of dates held.
     */
    public String getDateRange() {
        String toReturn = printDate(date1);
        if (date2 != null) {
            toReturn += " -> " + printDate(date2);
        }
        return toReturn;
    }

    /**
     * A list of the Results held by this Range. These are Results that have the same exact TimelineDate range of dates
     * as this Range (ie date1 and date2 of the Results and this Range are equal).
     *
     * @return the list of Results held by this Range.
     */
    public List<Result> getResults() {
        return results;
    }
}
