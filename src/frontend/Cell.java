package frontend;

import backend.process.Result;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.Label;
import javafx.scene.layout.VBox;

import java.io.IOException;

/**
 * Created by Oliver on 15/01/2017.
 */
public class Cell {
    @FXML
    private VBox vBox;
    @FXML
    private Label date;
    @FXML
    private Label subject;
    @FXML
    private Label event;

    public Cell(){
        FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("res/listCell.fxml"));
        fxmlLoader.setController(this);
        try {
            fxmlLoader.load();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void setInfo(Result result){
        System.out.println(date+" "+result);
        date.setText(result.getTimelineDate().toString());
        subject.setText(result.getSubjects().toString());
        event.setText(result.getEvent());
    }

    public VBox getvBox(){
        return vBox;
    }
}
