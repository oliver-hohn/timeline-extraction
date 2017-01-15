package frontend.controllers;

import backend.process.Result;
import frontend.ListViewCell;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.util.Callback;

import java.net.URL;
import java.util.ArrayList;
import java.util.ResourceBundle;

/**
 * Created by Oliver on 15/01/2017.
 */
public class ListViewController implements Initializable{
    @FXML
    private ListView listView;
    private ArrayList<Result> results;
    private ObservableList observableList = FXCollections.observableArrayList();
    private int x=0;


    @Override
    public void initialize(URL location, ResourceBundle resources) {
        System.out.println("Initialised listView");
    }

    public void setListView(ArrayList<Result> results){
        this.results = results;
        observableList.addAll(results);
        listView.setItems(observableList);
        listView.setCellFactory(new Callback<ListView, ListCell>() {
            @Override
            public ListCell call(ListView param) {
                System.out.println("List View Cell called: "+x);
                x++;
                return new ListViewCell();
            }
        });
    }
}
