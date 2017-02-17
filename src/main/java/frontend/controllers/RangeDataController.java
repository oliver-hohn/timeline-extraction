package frontend.controllers;

import backend.process.Result;
import backend.ranges.Range;
import frontend.observers.TimelineRowObserver;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.Label;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.scene.layout.Pane;
import javafx.scene.layout.VBox;
import javafx.util.Callback;

import java.io.IOException;

/**
 * Controller for the layout that represents the data held by a Range (i.e. its Data and its Results).
 */
public class RangeDataController {
    private Range range;
    @FXML
    private VBox rootVBox;
    @FXML
    private ListView<Result> resultsListView;
    @FXML
    private Label dateLabel;
    private ObservableList<Result> results;
    private TimelineRowObserver timelineRowObserver;
    private int rangePosition;

    /**
     * Constructor that sets up the layout to represent the data held by the given Range.
     *
     * @param range the given Range.
     */
    public RangeDataController(Range range, TimelineRowObserver timelineRowObserver, int rangePosition) {
        this.range = range;
        this.timelineRowObserver = timelineRowObserver;
        this.rangePosition = rangePosition;
        FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("rangeDataLayout.fxml"));//load the base layout
        fxmlLoader.setController(this);
        try {
            rootVBox = fxmlLoader.load();
            setUp();//set the data in the layout
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Set up the data shown in this layout. This means setting the date that this Range layout is representing, and the
     * list of Result objects held by the given Range (in the constructor).
     */
    private void setUp() {
        if (range != null) {
            dateLabel.setText(range.getDateRange());//set the date text
            if (range.getResults().size() > 0) {//if we have results then show the list
                results = FXCollections.observableArrayList();
                results.addAll(range.getResults());//set up the observable list of Results used to add to the listView
                resultsListView.setItems(results);
                resultsListView.setCellFactory(new Callback<ListView<Result>, ListCell<Result>>() {
                    @Override
                    public ListCell<Result> call(ListView<Result> param) {
                        return new ListCell<Result>() {//set up the listcell used in this listview
                            @Override
                            protected void updateItem(Result item, boolean empty) {
                                super.updateItem(item, empty);
                                if (item != null) {//if we have a valid item, build its layout and set it
                                    CustomResultRowController customResultRowController = new CustomResultRowController(item, timelineRowObserver, rangePosition);
                                    setGraphic(customResultRowController.getRootLayout());//set the layout built with the result
                                } else {//dont have a valid result so dont show anything
                                    setGraphic(null);
                                }
                            }
                        };
                    }
                });
            } else {//dont have results then dont show the list
                rootVBox.getChildren().remove(resultsListView);
            }
        }
    }

    /**
     * Get the root layout that this Controller controls (ie a VBox with the Date of the given Range - passed in the
     * constructor; and a list of the Results held by the Range).
     *
     * @return the root layout that this Controller represents.
     */
    public Pane getRootBorderPane() {
        return rootVBox;
    }


}
