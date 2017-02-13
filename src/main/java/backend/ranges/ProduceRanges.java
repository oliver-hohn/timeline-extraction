package backend.ranges;

import backend.process.Result;

import java.util.*;

/**
 * Produces a List of Range (that contain ranges - like a Tree, so it Returns a Forest) to use in showing the Timeline.
 */
public class ProduceRanges {
    private List<Range> trees = new ArrayList<>();

    public List<Range> getTrees() {
        return trees;
    }

    public void produceRanges(List<Result> resultList){
        sortByRange(resultList);
        //from the top of sorted list try to add to existing tree, if not possible make new tree and to list of trees (fores)
        makeForest(resultList);
        System.out.println("\nPrinting trees");
        for(Range range: trees){
            System.out.println(range);
        }
        System.out.println("\nFinished trees");
        sortForest();
    }

    private List<Result> sortByRange(List<Result> inputResults){
        Collections.sort(inputResults);//java +7 mergesort with O(nlogn) but if its almost sorted its closer to O(n)
        for(Result result: inputResults){
            System.out.println("For: "+result+" range: "+result.getTimelineDate().getRange());
        }
        return inputResults;
    }

    private void makeForest(List<Result> sortedResults){
        //TODO: make forest (includes Tree classes)

            //if have to update parent, make copy of current, then reset, then set copy as child
        for(Result result: sortedResults){//should be sorted from largest range to lowest
            //try to add to one of the trees if not possible make new range
            if(!addToRange(result)) {
                //we couldnt add it so we make a new Range
                Range range = new Range(result.getTimelineDate().getDate1(), result.getTimelineDate().getDate2());
                range.add(result);
                trees.add(range);
            }
        }
    }

    private boolean addToRange(Result result){
        for (Range range: trees){
            if(range.add(result)){
                return true;
            }
        }
        return false;
    }

    private void sortForest(){
        //TODO: sort trees
    }


}
