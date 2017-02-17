package frontend.controllers;

import backend.ranges.Range;
import javafx.fxml.FXMLLoader;
import javafx.geometry.HPos;
import javafx.geometry.VPos;
import javafx.scene.control.Label;
import javafx.scene.layout.*;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

//TODO: fxml layout that is loaded and added recursively
/*
with min height and width, with rows being added depending on number of ranges (listview of results at each row), and their recursive
 gridpane children in col 1
and col span remaining
with undefined max height and width
 */
public class CustomTimelineRow {
    private Range root;
    private GridPane rootLayout;
    public CustomTimelineRow(Range root){
        this.root = root;
        setUpLayout();
    }
    //so fxml file of gridpane with min height and width (recursively loaded to add)

    private void setUpLayout(){
        ArrayList<Range> list = new ArrayList<>();
        list.add(root);
        rootLayout = getGridPane(list);

    }

    private GridPane getGridPane(List<Range> rangeList){
        GridPane gridPane = loadGridPane();
        if(gridPane != null) {

            for (int i = 0; i < rangeList.size(); i++) {
                System.out.println("Making layout " + i);
                //layout for this Range
                Range range = rangeList.get(i);
                System.out.println(range);
                Pane toAdd = rangeDataLayout(range);
                gridPane.add(toAdd, 0, i);
                GridPane.setValignment(toAdd, VPos.TOP);
                GridPane.setHalignment(toAdd, HPos.LEFT);
                if (range.getChildren().size() > 0) {
                    GridPane gridPane1 = getGridPane(range.getChildren());
                    gridPane.add(gridPane1, 1, i);
                    //gridPane.setVgrow(gridPane1, Priority.ALWAYS);

                }
            }
        }
        return gridPane;
    }

    private Pane rangeDataLayout(Range range){
        RangeDataController rangeDataController = new RangeDataController(range);
        return rangeDataController.getRootBorderPane();
    }

    private GridPane loadGridPane(){
        GridPane gridPane = new GridPane();
        gridPane.setPrefSize(GridPane.USE_COMPUTED_SIZE, GridPane.USE_COMPUTED_SIZE);
        gridPane.setMinSize(GridPane.USE_PREF_SIZE, GridPane.USE_PREF_SIZE);
        gridPane.setMaxSize(Double.MAX_VALUE, Double.MAX_VALUE);
        return gridPane;
    }

    //TODO: load base gridpane layout and onto its fields the data
    //TODO: layout of individual range (date + listview)

    public Pane getPane(){
        return rootLayout;
    }

}
