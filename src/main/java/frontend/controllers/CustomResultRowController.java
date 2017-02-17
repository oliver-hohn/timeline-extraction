package frontend.controllers;

import backend.process.Result;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.Pane;
import javafx.scene.layout.VBox;

import java.io.IOException;

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

    /**
     * For the given Result, set up the data to be shown for this row, and the onclick events.
     * Builds a layout that shows the subjects, and the event of the given Result, along with buttons to view the
     * original document that produced this Result and the edit dialog to edit the Result.
     *
     * @param result the given Result.
     */
    public CustomResultRowController(Result result) {
        this.result = result;
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
                System.out.println("Clicked View on: " + result);
            }
        });

        editButton.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {
                System.out.println("Clicked Event on: " + result);
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
