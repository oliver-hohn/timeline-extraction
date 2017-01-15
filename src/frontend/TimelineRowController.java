package frontend;

import backend.process.Result;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Group;
import javafx.scene.control.Label;
import javafx.scene.layout.VBox;
import javafx.scene.text.Text;

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


    public TimelineRowController(){
        FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("res/timelinerow.fxml"));
        fxmlLoader.setController(this);
        try {
            fxmlLoader.load();
        } catch (IOException e) {
            e.printStackTrace();
        }
        //setData(result);
    }

    public void setData(Result result){
        dateLabel.setText(result.getTimelineDate().toString());
        subjectsLabel.setText(result.getSubjects().toString());
        eventLabel.setText(result.getEvent());
        timelineVBox.setStyle("-fx-border-color: black;" +
                "-fx-border-width: 4;" +
                "-fx-border-style: solid inside;");
    }

    public Group getGroup(){
        return group;
    }
}
