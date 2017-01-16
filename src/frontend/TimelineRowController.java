package frontend;

import backend.process.Result;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Group;
import javafx.scene.control.Label;
import javafx.scene.layout.VBox;

import java.io.IOException;

/**
 * Created by Oliver on 15/01/2017.
 */
public class TimelineRowController {
    @FXML
    private Group group;
    @FXML
    private VBox timelineVBox;
    @FXML
    private Label eventNumberLabel;
    @FXML
    private Label dateLabel;
    @FXML
    private Label subjectsLabel;
    @FXML
    private Label eventLabel;
    private int position;

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
        //setData(result);
    }

    public void setData(Result result){
        eventNumberLabel.setText("Event #"+(position+1));
        dateLabel.setText("Date: "+result.getTimelineDate().toString());
        subjectsLabel.setText("Subjects: "+result.getSubjects().toString());
        eventLabel.setText("Event: "+result.getEvent());
        timelineVBox.setStyle("-fx-border-color: black;" +
                "-fx-border-width: 4;" +
                "-fx-border-style: solid inside;");
    }

    public Group getGroup(){
        return group;
    }
}
