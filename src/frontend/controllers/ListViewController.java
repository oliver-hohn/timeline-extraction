package frontend.controllers;

import backend.process.Result;
import frontend.ListViewCell;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.util.Callback;

import java.net.URL;
import java.util.ArrayList;
import java.util.ResourceBundle;

/**
 * Controller for the layout where the ListView is shown. Allows the listview to be populated with Result data.
 */
public class ListViewController implements Initializable {
    @FXML
    private ListView listView;
    private ArrayList<Result> results;
    private ObservableList<Result> observableList = FXCollections.observableArrayList();

    /**
     * Called when the layout is created.
     */
    @Override
    public void initialize(URL location, ResourceBundle resources) {
        System.out.println("Initialised listView");
    }

    /**
     * For the given Results, populate the listView with them.
     *
     * @param results a list of Result objects which contain data to populate the rows of the listView with.
     */
    public void setListView(ArrayList<Result> results) {
        this.results = results;
        observableList.addAll(results);
        listView.setItems(observableList);
        listView.setCellFactory(new Callback<ListView, ListCell>() {
            @Override
            public ListCell call(ListView param) {
                return new ListViewCell();
            }
        });
    }
}
