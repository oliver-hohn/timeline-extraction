package frontend.controllers;

import backend.process.Result;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.VBox;

import java.io.IOException;

/**
 * Created by Oliver on 17/02/2017.
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
    public CustomResultRowController(Result result){
        this. result = result;
        FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("customResultRow.fxml"));
        fxmlLoader.setController(this);
        try {
            rootVBox = fxmlLoader.load();
            setUpData();
            setUpOnClicks();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void setUpData(){
        System.out.println("Setting Custom Result row for: "+result);
        if(result != null){
            subjectsLabel.setText(result.getSubjectsAsString());
            eventLabel.setText(result.getEvent());

        }
    }

    private void setUpOnClicks(){
        viewButton.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {
                System.out.println("Clicked View on: "+result);
            }
        });

        editButton.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {
                System.out.println("Clicked Event on: "+result);
            }
        });
    }

    public VBox getRootLayout(){
        return rootVBox;
    }
}
