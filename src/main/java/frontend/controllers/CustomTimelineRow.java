package frontend.controllers;

import backend.ranges.Range;
import frontend.observers.TimelineRowObserver;
import javafx.geometry.HPos;
import javafx.geometry.Orientation;
import javafx.geometry.VPos;
import javafx.scene.control.Separator;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Pane;

import java.util.ArrayList;
import java.util.List;

//TODO: picker for what timeline layout to use


/**
 * Class for the Custom Rows in the Timeline. Builds a row made of a GridPane with the root Range (given in the
 * constructor) and recursively adds to the other coloumn the Ranges below this Range.
 */
public class CustomTimelineRow {
    private Range root;
    private GridPane rootLayout;
    private TimelineRowObserver timelineRowObserver;

    /**
     * Constructor used to set up the layout of the Timeline row. Afterwards a call to getPane() will return the row
     * to be added to the Timeline.
     *
     * @param root the root Range (which can hold Ranges as children which are added Recursively to the 2nd column of
     *             the GridPane.
     */
    public CustomTimelineRow(Range root, TimelineRowObserver timelineRowObserver) {
        this.timelineRowObserver = timelineRowObserver;
        this.root = root;
        setUpLayout();//set up the layout of this Timeline Row
    }

    /**
     * Called to create the Row based on the given Range (given to the constructor). The RootLayout (a root GridPane)
     * will have then been set.
     */
    private void setUpLayout() {
        ArrayList<Range> list = new ArrayList<>();
        list.add(root);
        rootLayout = getGridPane(list, false);//get the root GridPane
        rootLayout.setGridLinesVisible(true);//add grid lines to the root only (for optical purposes)
    }

    /**
     * For the given list of Range objects, add them to the first column of the GridPane (that will be returned), if we
     * need to add the vertical Separator (given by addVerticalSep) add it to the second column of the GridPane. If we
     * have multiple Ranges in the RangeList then they are separated by a horizontal Separator.
     * Recursively adds to the children of each Range in the list, in the third column of the row of their Parent, this
     * way the Parent contains all of its children in its row (and the children do the same for their children, etc).
     * <p>
     * Range data in column 1 and row i (given by their index in the list).
     * Children Range's (if they have) in column 2  and row i (same as parent).
     *
     * @param rangeList      the given list of Range objects.
     * @param addVerticalSep whether or not we need to add a Vertical Separator
     * @return the GridPane built based on the given list of Range objects.
     */
    private GridPane getGridPane(List<Range> rangeList, boolean addVerticalSep) {
        GridPane gridPane = loadGridPane();//get the root GridPane layout that we are adding to
        if (gridPane != null) {
            for (int i = 0; i < 2 * rangeList.size() - 1; i++) {
                //add a separator between them
                if (i % 2 == 1) {
                    Separator separator = new Separator(Orientation.HORIZONTAL);
                    gridPane.add(separator, 0, i, GridPane.REMAINING, 1);
                } else {
                    //layout for this Range
                    Range range = rangeList.get(i / 2);
                    Pane toAdd = rangeDataLayout(range, (i/2));//add the layout for this given Range
                    gridPane.add(toAdd, 0, i);
                    GridPane.setValignment(toAdd, VPos.TOP);//set the items added to top left of the layout
                    GridPane.setHalignment(toAdd, HPos.LEFT);
                    if (range.getChildren().size() > 0) {//if this Range has children, then build their layout (recursively) and add them
                        if (addVerticalSep) {//we need to add a separator
                            Separator separator = new Separator(Orientation.VERTICAL);
                            gridPane.add(separator, 1, i);//add the separator
                        }
                        GridPane gridPane1 = getGridPane(range.getChildren(), true);
                        gridPane.add(gridPane1, 2, i);
                    }
                }
            }
        }
        return gridPane;//return the gridpane layout we added to
    }

    /**
     * Get the layout to show the data for the given Range (ie its Date range and its Results)
     *
     * @param range the given Range.
     * @return the layout representing the data of the given Range.
     */
    private Pane rangeDataLayout(Range range, int position) {
        RangeDataController rangeDataController = new RangeDataController(range, timelineRowObserver, position);
        return rangeDataController.getRootBorderPane();
    }

    /**
     * Get the base GridPane used to add a Range's data and its Children data (recursively). The GridPane is of the size
     * of its content, but it can be increased to any size.
     *
     * @return a base GridPane used to add a Range's data and its Children data.
     */
    private GridPane loadGridPane() {
        GridPane gridPane = new GridPane();
        gridPane.setPrefSize(GridPane.USE_COMPUTED_SIZE, GridPane.USE_COMPUTED_SIZE);
        gridPane.setMinSize(GridPane.USE_PREF_SIZE, GridPane.USE_PREF_SIZE);
        gridPane.setMaxSize(Double.MAX_VALUE, Double.MAX_VALUE);
        return gridPane;
    }


    /**
     * Get the root layout of this row in the Timeline (i.e. a GridPane that holds the information of the Root range,
     * passed in the timeline, in the first column with the Roots childrens information in the second column, and them
     * holding their children in their columns and so on).
     *
     * @return the root layout of a row in the Timeline given by the Range passed into the constructor.
     */
    public Pane getPane() {
        return rootLayout;
    }

}
