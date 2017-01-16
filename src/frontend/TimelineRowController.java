package frontend;

import backend.process.Result;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Group;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.VBox;

import java.io.IOException;

/**
 * Created by Oliver on 15/01/2017.
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
    private int position;
    private Result result;

    public TimelineRowController(int position){
        this.position = position;
        FXMLLoader fxmlLoader;
        boolean isEven = (position%2) == 0;
        if(isEven) {
            fxmlLoader = new FXMLLoader(getClass().getResource("res/timelineRowEven.fxml"));
        }else{
            fxmlLoader = new FXMLLoader(getClass().getResource("res/timelineRowOdd.fxml"));
        }
        fxmlLoader.setController(this);
        try {
            fxmlLoader.load();
        } catch (IOException e) {
            e.printStackTrace();
        }

        System.out.println("Edit: "+editButton);
        System.out.println("View: "+viewButton);
    }

    public void setData(Result result){
        this.result = result;
        eventNumberLabel.setText("Event #"+(position+1));
        dateLabel.setText("Date: "+result.getTimelineDate().toString());
        subjectsLabel.setText("Subjects: "+result.getSubjects().toString());
        eventLabel.setText("Event: "+result.getEvent());
        borderPane.setStyle("-fx-border-color: black; -fx-border-width: 4; -fx-border-style: solid inside;");
        editButton.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {
                System.out.println("Edit button for timeline event: "+result.getTimelineDate()+" has been pressed");
                //TODO: edit button implementation (edit dialog screen)
            }
        });
        viewButton.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {
                System.out.println("Go to Document button for timeline event: "+result.getTimelineDate()+" has been pressed");
                //TODO: go to document implementation (file reader with related sentence highlighted).
            }
        });
    }

    public Group getGroup(){
        return group;
    }
}
