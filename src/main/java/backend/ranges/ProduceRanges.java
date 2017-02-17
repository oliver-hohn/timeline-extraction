package backend.ranges;

import backend.process.Result;

import java.util.*;

/**
 * Produces a List of Range (that contain ranges - like a Tree, so it Returns a Forest) to use in showing the Timeline.
 */
public class ProduceRanges {
    private List<Range> trees = new ArrayList<>();

    /**
     * Get the list of Range Trees formed by processing the list of the Results.
     *
     * @return a list of Range Trees.
     */
    public List<Range> getTrees() {
        return trees;
    }

    /**
     * For the given Result list, sort them by size of range of dates held by their TimelineDate, then make a Forest of
     * Range Trees using the list of Results, and then finally sort the Forest by their date1 value (descending)
     *
     * @param resultList the given Result list.
     */
    public void produceRanges(List<Result> resultList) {
        System.out.println("Number of Results: " + resultList.size());
        sortByRange(resultList);//sort the list by their Range
        makeForest(resultList);//from the sorted list attempt to add it to existing Range Trees or create a new Tree if not possible.
        sortForest();//sort the Range (recursively) Forest by their date1 value
    }

    /**
     * For the given list of Results, sort them by the number of days in between their first range of dates and second.
     *
     * @param inputResults the given list of Results.
     * @return the list of Results sorted by their Range (descending)
     */
    private List<Result> sortByRange(List<Result> inputResults) {
        Collections.sort(inputResults);//java +7 mergesort with O(nlogn) but if its almost sorted its closer to O(n)
        Collections.reverse(inputResults);//as the list has been sorted in ascending order and we want descending
        System.out.println("Sorted by Range");
        for(Result result: inputResults){
            System.out.println(result+" "+result.getTimelineDate().getRange());
        }
        return inputResults;
    }

    /**
     * For the given sorted list of Results (has to be sorted for this algorithm to work properly as we are making the
     * Range trees starting from the highest Range) attempt to add each Result to existing Range trees, if that is not
     * possible then create a new Range tree for the Result.
     *
     * @param sortedResults the list of sorted Result objects.
     */
    private void makeForest(List<Result> sortedResults) {
        //if have to update parent, make copy of current, then reset, then set copy as child
        for (Result result : sortedResults) {//should be sorted from largest range to lowest
            //try to add to one of the trees if not possible make new range
            if (!addToRange(result)) {
                //we couldnt add it so we make a new Range
                Range range = new Range(result.getTimelineDate().getDate1(), result.getTimelineDate().getDate2());
                range.add(result);
                trees.add(range);
            }
        }
    }

    /**
     * Attempt to add the given Result to the the different Range trees.
     *
     * @param result the given Result.
     * @return true if the Result was added to one of the existing Range trees; false otherwise.
     */
    private boolean addToRange(Result result) {
        for (Range range : trees) {
            if (range.add(result)) {
                return true;
            }
        }
        return false;
    }

    /**
     * For the forest of Range trees recursively sort the trees (and their subtrees) by their date1 in ascending order.
     */
    private void sortForest() {
        //sort the roots first
        Collections.sort(trees);
        //for all the roots call sortChildren (which will recursively sort)
        for (Range root : trees) {
            root.sortChildren();
        }
    }


}
