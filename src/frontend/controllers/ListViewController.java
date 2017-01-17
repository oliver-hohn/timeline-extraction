package frontend.controllers;

import backend.process.FileData;
import backend.process.Result;
import frontend.DocumentLoadedRowController;
import frontend.ListViewCell;
import frontend.observers.DocumentsLoadedObserver;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.scene.layout.VBox;
import javafx.util.Callback;

import java.net.URL;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.ResourceBundle;

/**
 * Controller for the layout where the ListView is shown. Allows the listview to be populated with Result data.
 */
public class ListViewController implements Initializable, MenuBarControllerInter, DocumentsLoadedObserver {
    @FXML
    private ListView timelineListView;
    @FXML
    private Button loadDocumentsButton;
    @FXML
    private Button saveToPDFButton;
    @FXML
    private ListView documentListView;
    private ArrayList<Result> results;
    private ArrayList<FileData> fileDatas;
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

        loadDocumentsButton.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {
                System.out.println("Load in Documents");
            }
        });

        saveToPDFButton.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {
                System.out.println("Save to PDF");
            }
        });
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
        timelineObservableList.clear();
        timelineObservableList.addAll(results);
        timelineListView.setItems(timelineObservableList);
        timelineListView.setCellFactory(new Callback<ListView, ListCell>() {
            @Override
            public ListCell call(ListView param) {
                return new ListViewCell();
            }
        });

        //custom documents loaded listview
        this.fileDatas = fileDatas;
        documentsLoadedObservableList.clear();
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
                            DocumentLoadedRowController documentLoadedRowController = new DocumentLoadedRowController(item, ListViewController.this);
                            setGraphic(documentLoadedRowController.getGridPane());
                        }
                    }
                };
            }
        });

    }

    @Override
    public void close() {
        System.out.println("Close pressed");
    }

    @Override
    public void about() {
        System.out.println("About pressed");
    }

    @Override
    public void timeline() {
        System.out.println("Timeline pressed");
    }

    @Override
    public void remove(FileData fileData) {
        System.out.println("Need to remove: "+fileData);
        fileDatas.remove(fileData);
        removeResults(results, fileData);
        //remove from the Results the ones linked to this FileData
        setTimelineListView(results, fileDatas);
    }

    private void removeResults(ArrayList<Result> results, FileData fileData){
        Iterator<Result> resultIterator = results.iterator();
        while(resultIterator.hasNext()){
            Result result = resultIterator.next();
            if(result.getFileData().equals(fileData)){
                System.out.println("Need to remove: "+result);
                resultIterator.remove();
            }
        }
    }
}
