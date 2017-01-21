package frontend.controllers;

import backend.process.FileData;
import backend.process.Result;
import frontend.observers.DocumentsLoadedObserver;
import frontend.observers.TimelineObserver;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.util.Callback;

import java.net.URL;
import java.util.*;

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
    private TimelineObserver timelineObserver;

    /**
     * Called when the layout is created.
     */
    @Override
    public void initialize(URL location, ResourceBundle resources) {
        System.out.println("Initialised timelineListView");
        System.out.println("documentListView: " + documentListView);
        System.out.println("loadDocumentsButton: " + loadDocumentsButton);
        System.out.println("saveToPDFButton: " + saveToPDFButton);
    }

    /**
     * Called to set the Observer for this Scene.
     * @param timelineObserver the Observer for this Timeline Scene.
     */
    public void setTimelineObserver(TimelineObserver timelineObserver){
        this.timelineObserver = timelineObserver;
        loadDocumentsButton.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {
                System.out.println("Load in Documents");
                if(timelineObserver != null){
                    timelineObserver.loadDocumets();
                }
            }
        });

        saveToPDFButton.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {
                System.out.println("Save to PDF");
                if(timelineObserver != null){
                    timelineObserver.saveToPDF();
                }
            }
        });
    }

    /**
     * For the given Results, populate the timelineListView with them.
     *
     * @param results a list of Result objects which contain data to populate the rows of the timelineListView with.
     */
    public void setTimelineListView(ArrayList<Result> results, ArrayList<FileData> fileDatas) {
        this.results = results;
        Collections.sort(this.results);
        Collections.reverse(this.results);
        timelineObservableList.clear();
        timelineObservableList.addAll(this.results);
        timelineListView.setItems(timelineObservableList);
        timelineListView.setCellFactory(new Callback<ListView, ListCell>() {
            @Override
            public ListCell call(ListView param) {
                return new ListCell<Result>() {
                    /**
                     * Called whenever a row needs to be shown/created on the screen.
                     *
                     * @param result the Result object for which this row has to display data for.
                     * @param empty  whether or not the Row is empty (i.e. result == null).
                     */
                    @Override
                    protected void updateItem(Result result, boolean empty) {
                        super.updateItem(result, empty);
                        if (result != null) {
                            System.out.println("Got Item: " + result);
                            System.out.println("Position: " + getIndex());
                            TimelineRowController timelineRowController = new TimelineRowController(getIndex());
                            timelineRowController.setData(result);
                            setGraphic(timelineRowController.getGroup());
                        }
                    }
                };
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
                return new ListCell<FileData>() {
                    @Override
                    protected void updateItem(FileData item, boolean empty) {
                        super.updateItem(item, empty);
                        if (item != null) {
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

    /**
     * Called when the Close menu item is pressed.
     */
    @Override
    public void close() {
        System.out.println("Close pressed");
        if(timelineObserver != null){
            timelineObserver.close();
        }
    }

    /**
     * Called when the About menu item is pressed.
     */
    @Override
    public void about() {
        System.out.println("About pressed");
        if(timelineObserver != null){
            timelineObserver.showAbout();
        }
    }

    /**
     * Called when the Timeline menu item is pressed.
     */
    @Override
    public void timeline() {
        System.out.println("Timeline pressed");
        if(timelineObserver != null){
            timelineObserver.timeline();
        }
    }

    /**
     * Called when a given row in the Loaded Documents listview is removed.
     *
     * @param fileData the FileData for the File where we want to remove its results from the Timeline.
     */
    @Override
    public void remove(FileData fileData) {
        System.out.println("Need to remove: " + fileData);
        fileDatas.remove(fileData);
        removeResults(results, fileData);
        //TODO: dialog to confirm deletion
        setTimelineListView(results, fileDatas);
    }

    /**
     * Removes the given FileData from the FileData list and all the Results linked to it in the Results list.
     *
     * @param results  list of Results for which we we need to delete the Results linked to the given FileData.
     * @param fileData FileData for which in the given Results list we need to remove the linked Results.
     */
    private void removeResults(ArrayList<Result> results, FileData fileData) {
        Iterator<Result> resultIterator = results.iterator();
        while (resultIterator.hasNext()) {
            Result result = resultIterator.next();
            if (result.getFileData().equals(fileData)) {
                System.out.println("Need to remove: " + result);
                resultIterator.remove();
            }
        }
    }
}
