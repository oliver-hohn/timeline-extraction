package frontend.controllers;

import backend.process.FileData;
import backend.process.Result;
import frontend.RemoveConfirmationDialog;
import frontend.observers.DocumentsLoadedObserver;
import frontend.observers.TimelineObserver;
import frontend.observers.TimelineRowObserver;
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
//TODO: dont add repeated files

/**
 * Controller for the layout where the ListView is shown. Allows the listview to be populated with Result data.
 */
public class ListViewController implements Initializable, MenuBarControllerInter, DocumentsLoadedObserver, TimelineRowObserver {
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
                    timelineObserver.saveToPDF(results);
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
                            TimelineRowController timelineRowController = new TimelineRowController(getIndex(), ListViewController.this);
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
        cleanRepeatedResults(results, this.fileDatas);
        cleanRepeatedFileData(this.fileDatas, fileDatas);
        this.results.addAll(results);
        sortAndReverse(this.results);
        setTimelineList(this.results);

        this.fileDatas.addAll(fileDatas);
        Collections.sort(this.fileDatas);
        setDocumentListView(this.fileDatas);
    }

    /**
     * Called to remove from the newFileData list, all the FileData objects that are already present in the oldFileData.
     *
     * @param oldFileData the given oldFileData.
     * @param newFileData the given newFileData.
     */
    private void cleanRepeatedFileData(List<FileData> oldFileData, List<FileData> newFileData) {
        Iterator<FileData> newFileDataIterator = newFileData.iterator();
        while (newFileDataIterator.hasNext()) {//for each new result
            FileData fileData = newFileDataIterator.next();
            if (oldFileData.contains(fileData)) {//if its in the old list
                newFileDataIterator.remove();//then dont add it to it (so remove it)
            }
        }
    }

    /**
     * Called to remove all the Result objects in results list that have their FileData object present in the fileDatas
     * List.
     *
     * @param results   the given Result list.
     * @param fileDatas the given FileData list.
     */
    private void cleanRepeatedResults(List<Result> results, List<FileData> fileDatas) {
        Iterator<Result> resultIterator = results.iterator();
        while (resultIterator.hasNext()) {
            //for each result, check it with the filedata, if its filedata is already there, then remove it
            Result result = resultIterator.next();
            if (fileDatas.contains(result.getFileData())) {
                //this file data already exists, so remove the result
                resultIterator.remove();
                System.out.println("Removed: " + result + " from results to be added");
            }
        }
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

    /**
     * Called by a member of the ListView, to inform the ListView to update, as it updated its values.
     * The updated list can have this new Result row somewhere else as the user could have changed its date (and the list
     * is sorted by dates).
     *
     * @param updatedResult the Result object of the row that was edited.
     * @param position      the position this cell was in the ListView.
     */
    @Override
    public void update(Result updatedResult, int position) {
        System.out.println("Update the list");
        if (results.size() > position) {
            results.set(position, updatedResult);
            setTimelineListView(results, fileDatas);
        }

    }
}
