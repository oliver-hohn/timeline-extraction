package frontend.controllers;

import backend.process.Result;
import frontend.dialogs.EditEventDialog;
import frontend.observers.DocumentReaderObserver;
import frontend.observers.TimelineRowObserver;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Group;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Dialog;
import javafx.scene.control.Label;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.Pane;
import javafx.stage.Stage;

import java.io.IOException;
import java.util.Optional;
import java.util.function.Consumer;

/**
 * Controller class for each row in the timline listview.
 */
public class TimelineRowController {
    @FXML
    private Group group;
    @FXML
    private Label eventNumberLabel;
    @FXML
    private Label dateLabel;
    @FXML
    private Label subjectsLabel;
    @FXML
    private Label eventLabel;
    @FXML
    private BorderPane borderPane;
    @FXML
    private Button editButton;
    @FXML
    private Button viewButton;
    @FXML
    private Label fromLabel;
    private int position;
    private Result result;
    private TimelineRowObserver timelineRowObserver;

    /**
     * Loads the layout for the row (appropriate layout picked depending on whether row is even or not).
     *
     * @param position the position this row is in the timeline (to determine if its odd or even and to display its index).
     */
    public TimelineRowController(int position, TimelineRowObserver timelineRowObserver) {
        this.position = position;
        this.timelineRowObserver = timelineRowObserver;
        FXMLLoader fxmlLoader;
        boolean isEven = (position % 2) == 0;
        if (isEven) {
            fxmlLoader = new FXMLLoader(getClass().getResource("timelineRowEven.fxml"));
        } else {
            fxmlLoader = new FXMLLoader(getClass().getResource("timelineRowOdd.fxml"));
        }
        fxmlLoader.setController(this);
        try {
            fxmlLoader.load();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * For the row, where the layout has already been loaded, set the values at the labels, and the event handlers for
     * the buttons.
     *
     * @param result the Result object with wich the values of the labels will be populated (e.g. date, subjects, event,
     *               etc.).
     */
    public void setData(Result result) {
        this.result = result;
        eventNumberLabel.setText("Event #" + (position + 1));
        dateLabel.setText("Date: " + result.getTimelineDate().toString());
        subjectsLabel.setText("Subjects: " + result.getSubjectsAsString());
        eventLabel.setText("Event: " + result.getEvent());
        fromLabel.setText("From: " + result.getFileData().getFileName() + " (" + result.getFileData().getCreationDateFormattedDayMonthYear() + ")");
        borderPane.setStyle("-fx-border-color: black; -fx-border-width: 4; -fx-border-style: solid inside;");
        editButton.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {
                System.out.println("Edit button for timeline event: " + result.getTimelineDate() + " has been pressed");
                Dialog dialog = EditEventDialog.getEditEventDialog(result, (position + 1));
                Optional<EditEventDialog.DialogResult> response = dialog.showAndWait();
                response.ifPresent(new Consumer<EditEventDialog.DialogResult>() {
                    @Override
                    public void accept(EditEventDialog.DialogResult dialogResult) {
                        if (dialogResult.getResultType() == EditEventDialog.DialogResult.ResultType.DELETE) {
                            System.out.println("Delete the event");
                            timelineRowObserver.delete(position);
                        } else if (dialogResult.getResultType() == EditEventDialog.DialogResult.ResultType.SAVE) {
                            System.out.println("Update the timeline");
                            Result copy = dialogResult.getResult();
                            timelineRowObserver.update(copy, position);
                        } else if (dialogResult.getResultType() == EditEventDialog.DialogResult.ResultType.CANCEL) {
                            System.out.println("Dont do anything");
                        }
                    }
                });
            }
        });
        viewButton.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {
                System.out.println("Go to Document button for timeline event: " + result.getTimelineDate() + " has been pressed");
                Stage stage = new Stage();
                DocumentReaderController documentReaderController = new DocumentReaderController(result, new DocumentReaderObserver() {
                    @Override
                    public void close() {
                        System.out.println("Closing document reader window");
                        stage.close();
                    }
                });
                Pane rootLayout = documentReaderController.getRootBorderPane();
                if (rootLayout != null) {
                    stage.setScene(new Scene(documentReaderController.getRootBorderPane(), 1024, 800));
                    stage.setTitle("Document Reader - " + result.getFileData().getFileName());
                    stage.show();
                }
            }
        });
    }

    /**
     * After the layout has been loaded (and the data has been populated), return the layout to use it in the timeline.
     *
     * @return the parent layout of the row in the timeline.
     */
    public Group getGroup() {
        return group;
    }
}
