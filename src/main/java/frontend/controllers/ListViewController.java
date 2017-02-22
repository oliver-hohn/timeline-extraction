package frontend.controllers;

import backend.Sort;
import backend.process.FileData;
import backend.process.Result;
import backend.ranges.ProduceRanges;
import backend.ranges.Range;
import frontend.dialogs.LoadingDialog;
import frontend.dialogs.RemoveConfirmationDialog;
import frontend.observers.DocumentsLoadedObserver;
import frontend.observers.TimelineObserver;
import frontend.observers.TimelineRowObserver;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.geometry.Insets;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.input.ScrollEvent;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.util.Callback;

import java.net.URL;
import java.util.*;


/**
 * Controller for the layout where the ListView is shown. Allows the listview to be populated with Result data.
 */
public class ListViewController implements Initializable, MenuBarControllerInter, DocumentsLoadedObserver, TimelineRowObserver {
    private enum ViewType {
        RANGE, DATE
    }

    private final static double MAX_ZOOM = 1.0d;
    private final static double MIN_ZOOM = 0.75d;

    @FXML
    private StackPane stackPane;
    @FXML
    private VBox vBox;
    @FXML
    private ListView<Object> timelineListView;
    @FXML
    private Button loadDocumentsButton;
    @FXML
    private Button saveToButton;
    @FXML
    private ListView<FileData> documentListView;
    @FXML
    private RadioMenuItem dateView;
    @FXML
    private RadioMenuItem rangeView;
    @FXML
    private ScrollPane scrollPane;
    private List<Result> results;
    private List<FileData> fileDatas;
    private ObservableList<Object> timelineObservableList = FXCollections.observableArrayList();
    private ObservableList<FileData> documentsLoadedObservableList = FXCollections.observableArrayList();
    private TimelineObserver timelineObserver;
    private LoadingDialog loadingDialog;
    private ToggleGroup radioMenuItemGroup;
    private ViewType viewType = ViewType.DATE;


    /**
     * Called when the layout is created.
     */
    @Override
    public void initialize(URL location, ResourceBundle resources) {
        System.out.println("Initialised timelineListView");
        System.out.println("documentListView: " + documentListView);
        System.out.println("loadDocumentsButton: " + loadDocumentsButton);
        System.out.println("saveToButton: " + saveToButton);
        loadingDialog = new LoadingDialog(stackPane, vBox);//pass the root layout and main content layout to know where
        //to show the loading dialog, and what to disable.
        //set up the group of the radio buttons in the menu
        //by default the dateView is shown
        dateView.setSelected(true);
        radioMenuItemGroup = new ToggleGroup();
        rangeView.setToggleGroup(radioMenuItemGroup);
        dateView.setToggleGroup(radioMenuItemGroup);
        rangeView.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {
                showRangeTimeline();
            }
        });
        dateView.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {
                showDateTimeline();
            }
        });

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
        saveToButton.setText("Save \nTo...");
        saveToButton.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {
                if (timelineObserver != null) {
                    timelineObserver.saveTo(results);
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
        Sort.sortByDate1(results);//sort the results by their date1 value
        Collections.reverse(results);
    }

    /**
     * For the given Node make it visible and enable it depending on shouldShow. If shouldShow is true then make the
     * node visible and enable it; if it false make the node invisible and disable it.
     *
     * @param node       the given Node.
     * @param shouldShow whether or not the Node should be shown and enabled or not.
     */
    private void show(Node node, boolean shouldShow) {
        node.setVisible(shouldShow);
        node.setDisable(!shouldShow);
    }

    /**
     * For the input List of Results, set it as the items of the TimelineList. The type of timeline shown in the
     * ListView is given by the ViewType (member of the class that is changed with the RadioMenuItems).
     * <p>
     * The ListView and its ObservableList both set their generic type to Object, this way we don't have to create a
     * separate ListView when we want to swap from Results to Ranges (and vice versa). As when a ListView is set to
     * use a list of Ranges (for example) it will set its type to that, so then when it attempts to set a list of
     * Results it will throw an exception of the Casting failing (because its type is Ranges but its attempting to cast
     * it to Result).
     * <p>
     * When a row needs to be created from an item in that list, its type is looked at to determine what layout to use.
     * <p>
     * In order to change between Timeline Views use a ViewType that sets which View to use, where the RadioMenuItems
     * change its value according, with the default Timeline view being the Date Timeline (showing Results
     * individually). To allow the ListView to show different views of lists that each hold different kind of data, the
     * list and the observable had to be made of type Object as otherwise swapping from a list of Results to a list of
     * Ranges and setting them to the ListView would throw an exception of the Casting failing of a Range to a Result
     * (this also happens vice versa).
     * <p>
     * The timeline for the Range view is a ScrollPane that can be zoomed in/out.
     *
     * @param results the input List.
     */
    private void setTimelineList(List<Result> results) {
        //should show loading dialog while its setting the timeline
        timelineObservableList.clear();
        //check what kind of view we need to show
        if (viewType == ViewType.RANGE) {
            //we need to show the range view, which supports zooming in and out (so we need to use a vbox and scrollpane,
            //to be able to zoom)
            ProduceRanges produceRanges = new ProduceRanges();
            produceRanges.produceRanges(results);//produce the results

            VBox listVBox = new VBox();//the vbox that holds the Ranges
            listVBox.setPadding(new Insets(10));
            for (Range range : produceRanges.getTrees()) {//for each tree build its layout and add it to the vbox (row by row)
                CustomTimelineRow customTimelineRow = new CustomTimelineRow(range, this);
                listVBox.getChildren().add(customTimelineRow.getPane());
            }
            scrollPane.setContent(listVBox);//set the content of the scrollpane
            scrollPane.addEventFilter(ScrollEvent.ANY, new EventHandler<ScrollEvent>() {//add the event handler for the zooming
                @Override
                public void handle(ScrollEvent event) {
                    if (event.isControlDown()) {//if we are holding the control button
                        double scale = getScale(event, listVBox);//get the value by which we scale
                        listVBox.setScaleX(scale);//and set it
                        listVBox.setScaleY(scale);
                        event.consume();
                    }
                }

                private double getScale(ScrollEvent scrollEvent, Node node) {
                    double scale = node.getScaleX() + scrollEvent.getDeltaY() / 100;
                    if (scale <= MIN_ZOOM) {//we only want the user to zoom out (not in, hence the scale is never over 1)
                        scale = MIN_ZOOM;
                    } else if (scale >= MAX_ZOOM) {
                        scale = MAX_ZOOM;
                    }
                    return scale;
                }
            });
            show(scrollPane, true);//show the scrollpane
            show(timelineListView, false);//and hide the timeline list view
            return;//no need to follow the rest
        } else if (viewType == ViewType.DATE) {//if we have to show a date timeline
            show(scrollPane, false);//hide the scrollpane
            show(timelineListView, true);//show the timeline
            timelineListView.getStylesheets().setAll(getClass().getResource("listViewTheme.css").toExternalForm());
            timelineObservableList.setAll(results);//add the results
        }
        //assuming the observable list items have been set
        timelineListView.setItems(timelineObservableList);
        timelineListView.setCellFactory(new Callback<ListView<Object>, ListCell<Object>>() {
            @Override
            public ListCell<Object> call(ListView<Object> param) {
                return new ListCell<Object>() {
                    /**
                     * Called whenever a row needs to be shown/created on the screen.
                     * @param item the Range object for which this row has to be displayed for.
                     * @param empty whether or nor the row is empty.
                     */
                    @Override
                    protected void updateItem(Object item, boolean empty) {
                        super.updateItem(item, empty);
                        if (item != null && !empty) {
                            if (item instanceof Range) {
                                Range range = (Range) item;
                                CustomTimelineRow customTimelineRow = new CustomTimelineRow(range, ListViewController.this);
                                setGraphic(customTimelineRow.getPane());
                            } else if (item instanceof Result) {
                                Result result = (Result) item;
                                TimelineRowController timelineRowController = new TimelineRowController(getIndex(), ListViewController.this);
                                timelineRowController.setData(result);
                                setGraphic(timelineRowController.getGroup());
                            } else {
                                setGraphic(null);
                            }
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
        if (viewType == ViewType.DATE) {//to not waste time sorting Results that will be sorted by their Ranges later anyways
            sortAndReverse(this.results);
        }
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
     * When the preferences menu item is pressed.
     */
    @Override
    public void preferences() {
        if (timelineObserver != null) {
            timelineObserver.preferences();
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
        if (response.isPresent() && response.get() == ButtonType.YES) {
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

    /**
     * Called by a member of the ListView, to inform the ListView to update, as it updated its values.
     * The updated list can have this new Result row somewhere else as the user could have changed its date (and the list
     * is sorted by dates).
     *
     * @param previous      the Result object that was previously in this Row.
     * @param updatedResult the updated Result object of the TimelineRow.
     */
    @Override
    public void update(Result previous, Result updatedResult) {
        int pos = results.indexOf(previous);
        System.out.println("Previous: " + pos);
        if (pos != -1) {
            update(updatedResult, pos);
        }
    }

    /**
     * Called by a member of the ListView, to inform the ListView that it needs to be removed from the List.
     * Thereby the list needs to be updated (i.e. set again).
     *
     * @param position the position of the event that needs to be deleted.
     */
    @Override
    public void delete(int position) {
        System.out.println("Deleting the event");
        if (results.size() > position) {
            results.remove(position);
            setTimelineListView(results, fileDatas);
        }
    }

    /**
     * Called by a member of the ListView, to inform the ListView that it needs to be removed from the List.
     * Thereby the list needs to be updated (i.e. set again).
     *
     * @param result the given event (Result) to be deleted.
     */
    @Override
    public void delete(Result result) {
        int pos = results.indexOf(result);
        System.out.println("pos: " + pos);
        if (pos != -1) {
            delete(pos);
        }
    }

    /**
     * Called to show the loading dialog. (Only if the layouts have been passed to LoadingDialog)
     */
    public void showLoadingDialog() {
        loadingDialog.showLoadingDialog();
    }

    /**
     * Called to remove the loading dialog.
     */
    public void removeLoadingDialog() {
        loadingDialog.removeLoadingDialog();
    }

    /**
     * Called to show the Timeline with the individual dates and events in separate rows.
     */
    private void showDateTimeline() {
        if (viewType != ViewType.DATE) {
            viewType = ViewType.DATE;
            //show what is shown
            setTimelineListView(results, fileDatas);
        }
    }

    /**
     * Called to show the Timeline with the Results grouped into their ranges and then shown.
     */
    private void showRangeTimeline() {
        if (viewType != ViewType.RANGE) {
            viewType = ViewType.RANGE;
            setTimelineListView(results, fileDatas);
        }
    }
}
