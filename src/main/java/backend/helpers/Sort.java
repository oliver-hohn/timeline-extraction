package backend.helpers;

import backend.process.Result;

import java.util.Collections;
import java.util.Comparator;
import java.util.List;

/**
 * Class that holds the sorting methods used throughout the project.
 */
public class Sort {
    /**
     * Sort the given List by their Date1 (in their TimelineDate)
     *
     * @param resultList the given List.
     * @return the given List sorted in ascending order.
     */
    public static List<Result> sortByDate1(List<Result> resultList) {
        Collections.sort(resultList, new Comparator<Result>() {//will sort in ascending order
            @Override
            public int compare(Result o1, Result o2) {
                if (o1.getTimelineDate().getDate1() != null && o2.getTimelineDate().getDate1() != null) {
                    return o1.getTimelineDate().getDate1().compareTo(o2.getTimelineDate().getDate1());
                }
                if (o1.getTimelineDate().getDate1() == null) {
                    return -1;
                }
                return 1;
            }
        });
        return resultList;
    }
}
