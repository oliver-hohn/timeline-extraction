package frontend.controllers;

import backend.process.Result;
import backend.ranges.Range;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.Label;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.VBox;
import javafx.util.Callback;

import java.io.IOException;

/**
 * Created by Oliver on 17/02/2017.
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

    public RangeDataController(Range range){
        this.range = range;
        FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("rangeDataLayout.fxml"));
        fxmlLoader.setController(this);
        try {
            rootVBox = fxmlLoader.load();
            setUp();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void setUp(){
        if(range != null) {
            dateLabel.setText(range.getDateRange());
            if (range.getResults().size() > 0) {
                results = FXCollections.observableArrayList();
                results.addAll(range.getResults());
                resultsListView.setItems(results);
                resultsListView.setCellFactory(new Callback<ListView<Result>, ListCell<Result>>() {
                    @Override
                    public ListCell<Result> call(ListView<Result> param) {
                        return new ListCell<Result>() {
                            @Override
                            protected void updateItem(Result item, boolean empty) {
                                super.updateItem(item, empty);
                                System.out.println("IteM: " + item + " empty: " + empty);
                                if (item != null) {
                                    CustomResultRowController customResultRowController = new CustomResultRowController(item);
                                    setGraphic(customResultRowController.getRootLayout());
                                } else {
                                    setGraphic(null);
                                }
                            }
                        };
                    }
                });
            }else{
                rootVBox.getChildren().remove(resultsListView);
            }
        }
    }

    public VBox getRootBorderPane(){
        return rootVBox;
    }


}
