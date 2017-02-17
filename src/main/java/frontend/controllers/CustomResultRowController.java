package frontend.controllers;

import backend.process.Result;
import frontend.dialogs.EditEventDialog;
import frontend.observers.DocumentReaderObserver;
import frontend.observers.TimelineRowObserver;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Dialog;
import javafx.scene.control.Label;
import javafx.scene.layout.Pane;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

import java.io.IOException;
import java.util.Optional;
import java.util.function.Consumer;

/**
 * Controller for the custom Result rows in the ListView of the RangeData layout.
 */
public class CustomResultRowController {
    @FXML
    private VBox rootVBox;
    @FXML
    private Button viewButton;
    @FXML
    private Button editButton;
    @FXML
    private Label subjectsLabel;
    @FXML
    private Label eventLabel;
    private Result result;
    private TimelineRowObserver timelineRowObserver;
    private int rangePosition;
    /**
     * For the given Result, set up the data to be shown for this row, and the onclick events.
     * Builds a layout that shows the subjects, and the event of the given Result, along with buttons to view the
     * original document that produced this Result and the edit dialog to edit the Result.
     *
     * @param result the given Result.
     */
    public CustomResultRowController(Result result, TimelineRowObserver timelineRowObserver, int rangePosition) {
        this.result = result;
        this.timelineRowObserver = timelineRowObserver;
        this.rangePosition = rangePosition;
        FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("customResultRow.fxml"));
        fxmlLoader.setController(this);
        try {
            rootVBox = fxmlLoader.load();//set the root layout
            setUpData();//set the data
            setUpOnClicks();//set the onclicks
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Set the data held by the Result in the appropriate labels.
     */
    private void setUpData() {
        if (result != null) {
            subjectsLabel.setText(result.getSubjectsAsString());
            eventLabel.setText(result.getEvent());

        }
    }

    /**
     * Set the EventHandlers for the onClicks of the buttons of this Result row.
     */
    private void setUpOnClicks() {
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

        editButton.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {
                System.out.println("Clicked Event on: " + result);
                Dialog dialog = EditEventDialog.getEditEventDialog(result, (rangePosition + 1));
                Optional<EditEventDialog.DialogResult> response = dialog.showAndWait();
                response.ifPresent(new Consumer<EditEventDialog.DialogResult>() {
                    @Override
                    public void accept(EditEventDialog.DialogResult dialogResult) {
                        if (dialogResult.getResultType() == EditEventDialog.DialogResult.ResultType.DELETE) {
                            System.out.println("Delete the event");
                            timelineRowObserver.delete(result);
                        } else if (dialogResult.getResultType() == EditEventDialog.DialogResult.ResultType.SAVE) {
                            System.out.println("Update the timeline");
                            Result copy = dialogResult.getResult();
                            timelineRowObserver.update(result, copy);
                        } else if (dialogResult.getResultType() == EditEventDialog.DialogResult.ResultType.CANCEL) {
                            System.out.println("Dont do anything");
                        }
                    }
                });
            }
        });
    }

    /**
     * Get the root layout represented by this Controller (i.e. labels showing the subjects and events held by this
     * Result, and buttons to view the document that produced the Result and edit the Result.
     *
     * @return the root layout represented by this Controller.
     */
    public Pane getRootLayout() {
        return rootVBox;
    }
}
