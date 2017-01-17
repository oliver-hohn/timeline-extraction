package frontend.controllers;

import backend.process.FileData;
import backend.process.Result;
import frontend.DocumentLoadedRowController;
import frontend.ListViewCell;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.scene.layout.VBox;
import javafx.util.Callback;

import java.net.URL;
import java.util.ArrayList;
import java.util.ResourceBundle;

/**
 * Controller for the layout where the ListView is shown. Allows the listview to be populated with Result data.
 */
public class ListViewController implements Initializable {
    @FXML
    private ListView timelineListView;
    @FXML
    private VBox documentsLoadedVBox;
    @FXML
    private Button loadDocumentsButton;
    @FXML
    private Button saveToPDFButton;
    @FXML
    private ListView documentListView;
    private ArrayList<Result> results;
    private ObservableList<Result> timelineObservableList = FXCollections.observableArrayList();
    private ObservableList<FileData> documentsLoadedObservableList = FXCollections.observableArrayList();

    /**
     * Called when the layout is created.
     */
    @Override
    public void initialize(URL location, ResourceBundle resources) {
        System.out.println("Initialised timelineListView");
        System.out.println("documentListView: "+documentListView);
        System.out.println("loadDocumentsButton: "+loadDocumentsButton);
        System.out.println("saveToPDFButton: "+saveToPDFButton);
        System.out.println("documentsLoadedVBox: "+documentsLoadedVBox);
    }

    /**
     * For the given Results, populate the timelineListView with them.
     *
     * @param results a list of Result objects which contain data to populate the rows of the timelineListView with.
     */
    public void setTimelineListView(ArrayList<Result> results, ArrayList<FileData> fileDatas) {
        //TODO:
        //pass the load documents and save to pdf callbacks to observer
        this.results = results;
        timelineObservableList.addAll(results);
        timelineListView.setItems(timelineObservableList);
        timelineListView.setCellFactory(new Callback<ListView, ListCell>() {
            @Override
            public ListCell call(ListView param) {
                return new ListViewCell();
            }
        });

        //custom documents loaded listview
        documentsLoadedObservableList.addAll(fileDatas);
        documentListView.setItems(documentsLoadedObservableList);
        documentListView.setCellFactory(new Callback<ListView, ListCell>() {
            @Override
            public ListCell call(ListView param) {
                return new ListCell<FileData>(){
                    @Override
                    protected void updateItem(FileData item, boolean empty) {
                        super.updateItem(item, empty);
                        if(item != null){
                            System.out.println("Got Item: " + item);
                            System.out.println("Position: " + getIndex());
                            DocumentLoadedRowController documentLoadedRowController = new DocumentLoadedRowController();
                            documentLoadedRowController.setData(item);
                            setGraphic(documentLoadedRowController.getGridPane());
                        }
                    }
                };
            }
        });

    }
}
