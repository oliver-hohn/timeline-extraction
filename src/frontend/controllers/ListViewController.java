package frontend.controllers;

import backend.process.FileData;
import backend.process.Result;
import frontend.RemoveConfirmationDialog;
import frontend.observers.DocumentsLoadedObserver;
import frontend.observers.TimelineObserver;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.util.Callback;

import java.net.URL;
import java.util.*;

/**
 * Controller for the layout where the ListView is shown. Allows the listview to be populated with Result data.
 */
public class ListViewController implements Initializable, MenuBarControllerInter, DocumentsLoadedObserver {
    @FXML
    private ListView<Result> timelineListView;
    @FXML
    private Button loadDocumentsButton;
    @FXML
    private Button saveToPDFButton;
    @FXML
    private ListView<FileData> documentListView;
    private List<Result> results;
    private List<FileData> fileDatas;
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
     *
     * @param timelineObserver the Observer for this Timeline Scene.
     */
    public void setTimelineObserver(TimelineObserver timelineObserver) {
        this.timelineObserver = timelineObserver;
        loadDocumentsButton.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {
                System.out.println("Load in Documents");
                if (timelineObserver != null) {
                    timelineObserver.loadDocuments();
                }
            }
        });

        saveToPDFButton.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {
                System.out.println("Save to PDF");
                if (timelineObserver != null) {
                    timelineObserver.saveToPDF();
                }
            }
        });
    }

    /**
     * For the input List, sort and reverse it.
     * O(n)
     *
     * @param results the input List.
     */
    private void sortAndReverse(List<Result> results) {
        Collections.sort(results);
        Collections.reverse(results);
    }

    /**
     * For the input List of Results, set it as the items of the TimelineList.
     *
     * @param results the input List.
     */
    private void setTimelineList(List<Result> results) {
        timelineObservableList.clear();
        timelineObservableList.addAll(results);
        timelineListView.setItems(timelineObservableList);
        timelineListView.setCellFactory(new Callback<ListView<Result>, ListCell<Result>>() {
            @Override
            public ListCell<Result> call(ListView<Result> param) {
                return new ListCell<Result>() {
                    /**
                     * Called whenever a row needs to be shown/created on the screen.
                     *
                     * @param item the Result object for which this row has to display data for.
                     * @param empty  whether or not the Row is empty (i.e. result == null).
                     */
                    @Override
                    protected void updateItem(Result item, boolean empty) {
                        super.updateItem(item, empty);
                        if (item != null && !empty) {
                            TimelineRowController timelineRowController = new TimelineRowController(getIndex());
                            timelineRowController.setData(item);
                            setGraphic(timelineRowController.getGroup());
                        } else {
                            setGraphic(null);
                        }
                    }
                };
            }
        });
    }

    /**
     * For the input List of FileData, set it as the items of the Documents Loaded List.
     *
     * @param fileDatas the input List.
     */
    private void setDocumentListView(List<FileData> fileDatas) {
        documentsLoadedObservableList.clear();
        documentsLoadedObservableList.addAll(fileDatas);
        documentListView.setItems(documentsLoadedObservableList);
        documentListView.setCellFactory(new Callback<ListView<FileData>, ListCell<FileData>>() {
            @Override
            public ListCell<FileData> call(ListView<FileData> param) {
                return new ListCell<FileData>() {
                    @Override
                    protected void updateItem(FileData item, boolean empty) {
                        super.updateItem(item, empty);
                        if (item != null && !empty) {
                            DocumentLoadedRowController documentLoadedRowController = new DocumentLoadedRowController(item, ListViewController.this);
                            setGraphic(documentLoadedRowController.getGridPane());
                        } else {
                            setGraphic(null);
                        }
                    }
                };
            }
        });
    }

    /**
     * For the given Results and FileData, set the data of the timelineListView and documentListView with them.
     *
     * @param results   a list of Result objects which contain data to populate the rows of the timelineListView with.
     * @param fileDatas a list of FileData objects which needs to populate the rows in the documentListView.
     */
    public void setTimelineListView(List<Result> results, List<FileData> fileDatas) {
        this.results = results;
        sortAndReverse(this.results);
        setTimelineList(this.results);

        this.fileDatas = fileDatas;
        Collections.sort(this.fileDatas);
        setDocumentListView(this.fileDatas);
    }

    /**
     * For the given input, add it to their appropriate lists.
     *
     * @param results   a list of Result objects which contain data to add to the timelineListView.
     * @param fileDatas a list of FileData objects which needs to be added to the documentListView.
     */
    public void addToTimelineListView(List<Result> results, List<FileData> fileDatas) {
        this.results.addAll(results);
        sortAndReverse(this.results);
        setTimelineList(this.results);

        this.fileDatas.addAll(fileDatas);
        Collections.sort(this.fileDatas);
        setDocumentListView(this.fileDatas);
    }

    /**
     * Called when the Close menu item is pressed.
     */
    @Override
    public void close() {
        System.out.println("Close pressed");
        if (timelineObserver != null) {
            timelineObserver.close();
        }
    }

    /**
     * Called when the About menu item is pressed.
     */
    @Override
    public void about() {
        System.out.println("About pressed");
        if (timelineObserver != null) {
            timelineObserver.showAbout();
        }
    }

    /**
     * Called when the Timeline menu item is pressed.
     */
    @Override
    public void timeline() {
        System.out.println("Timeline pressed");
        if (timelineObserver != null) {
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
        Alert removeConfirmationDialog = RemoveConfirmationDialog.getRemoveConfirmationDialog(fileData.getFileName());
        Optional<ButtonType> response = removeConfirmationDialog.showAndWait();
        System.out.println("Text on clicked button: " + response.get().getText());
        if (response.get() == ButtonType.YES) {
            fileDatas.remove(fileData);
            removeResults(results, fileData);
            setTimelineListView(results, fileDatas);
        }//else dont remove the events related to that file (as we keep it)
    }

    /**
     * Removes the given FileData from the FileData list and all the Results linked to it in the Results list.
     *
     * @param results  list of Results for which we we need to delete the Results linked to the given FileData.
     * @param fileData FileData for which in the given Results list we need to remove the linked Results.
     */
    private void removeResults(List<Result> results, FileData fileData) {
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
