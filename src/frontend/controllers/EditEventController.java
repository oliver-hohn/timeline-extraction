package frontend.controllers;

import backend.process.Result;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.image.ImageView;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Set;

/**
 * Created by Oliver on 23/01/2017.
 */
public class EditEventController {
    @FXML
    private TextField firstDateTextField;
    @FXML
    private TextField secondDateTextField;
    @FXML
    private TextField subjectTextField;
    @FXML
    private ImageView addImageView;
    @FXML
    private TextArea eventTextArea;
    @FXML
    private HBox subjectsHBox;
    @FXML
    private GridPane rootGridPane;
    private Result result;
    private SimpleDateFormat simpleDateFormat = new SimpleDateFormat("dd-MM-yyyy");

    public EditEventController(Result result){
        this.result = result;
        FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("res/editEventDialog.fxml"));
        fxmlLoader.setController(this);
        try {
            fxmlLoader.load();
        } catch (IOException e) {
            e.printStackTrace();
        }
        setData();
        addListeners();
    }

    private void setData(){
        //for the set result object populate the fields
        if(result != null){
            firstDateTextField.setText(getStringFromDate(result.getTimelineDate().getDate1()));
            secondDateTextField.setText(getStringFromDate(result.getTimelineDate().getDate2()));
            subjectsHBox.getChildren().setAll(getSubjectLabels(result.getSubjects()));
            eventTextArea.setText(result.getEvent());
            firstDateTextField.setEditable(true);
            secondDateTextField.setEditable(true);
            eventTextArea.setEditable(true);
        }
    }

    private List<Label> getSubjectLabels(Set<String> subjects){
        List<Label> labels = new ArrayList<>();
        for(String subject: subjects){
            Label label = new Label(subject);
            labels.add(label);
        }
        return labels;
    }

    private String getStringFromDate(Date date){
        return (date != null) ? simpleDateFormat.format(date) : "";
    }

    private void addListeners(){
        //for the fields add their event listeners (eg iamgeview add to hbox of label)
    }

    public GridPane getRootGridPane(){
        return rootGridPane;
    }
}
