package frontend.controllers;

import backend.process.Result;
import frontend.TextFieldState;
import frontend.observers.EditEventDialogObserver;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.css.PseudoClass;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.*;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;

import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;

/**
 * Controller of the Content in the EditEventDialog
 */
//TODO: refactor, clone Result, char limit in textarea
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
    private SimpleDateFormat validInputFormat = new SimpleDateFormat("dd-MM-yyyy");
    private final PseudoClass errorClass = PseudoClass.getPseudoClass("error");
    private EditEventDialogObserver editEventDialogObserver;
    private ArrayList<TextFieldState> textFieldStates;


    /**
     * Constructor to create the layout for the Content of the EditEventDialog.
     *
     * @param result                  the Result object used to populate the fields of the layout of the Dialog content.
     * @param editEventDialogObserver the Observer of this content (the holder of the Dialog), to inform when to enable
     *                                and disable the buttons (which aren't part of the content of the dialog, but
     *                                separate).
     */
    public EditEventController(Result result, EditEventDialogObserver editEventDialogObserver) {
        this.result = result;
        this.editEventDialogObserver = editEventDialogObserver;
        textFieldStates = new ArrayList<>(2);
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

    /**
     * For the Result object that has been set, populate the fields.
     */
    private void setData() {
        //for the set result object populate the fields
        if (result != null) {
            firstDateTextField.setText(getStringFromDate(result.getTimelineDate().getDate1()));
            firstDateTextField.getStylesheets().add(getClass().getResource("res/customTextField.css").toExternalForm());
            textFieldStates.add(0, TextFieldState.CORRECT);
            secondDateTextField.setText(getStringFromDate(result.getTimelineDate().getDate2()));
            secondDateTextField.getStylesheets().add(getClass().getResource("res/customTextField.css").toExternalForm());
            textFieldStates.add(1, TextFieldState.CORRECT);
            subjectsHBox.getChildren().setAll(getSubjectLabels(result.getSubjects()));
            eventTextArea.setText(result.getEvent());
            firstDateTextField.setEditable(true);
            secondDateTextField.setEditable(true);
            eventTextArea.setEditable(true);
        }
    }

    /**
     * For the set of Strings (which should be Subjects) produce a list of Labels (to add to the HBox Subjects layout).
     *
     * @param subjects the set of Strings.
     * @return a list of Labels (corresponding to each each item in the set and a listener to show a dialog to remove the
     * subject from the Result).
     */
    private List<Label> getSubjectLabels(Set<String> subjects) {
        List<Label> labels = new ArrayList<>();
        for (String subject : subjects) {
            Label label = new Label(subject);
            label.setUnderline(true);
            label.setOnMouseClicked(new EventHandler<MouseEvent>() {
                @Override
                public void handle(MouseEvent event) {
                    System.out.println("Remove this label and subject from the Result: " + subject);
                    if (shouldDeleteSubjectLabel(subject)) {
                        System.out.println("Removed: " + subject);
                        result.getSubjects().remove(subject);
                        System.out.println("Remaining: " + result.getSubjects());
                        subjectsHBox.getChildren().setAll(getSubjectLabels(result.getSubjects()));

                    }
                }
            });
            labels.add(label);
        }
        return labels;
    }

    /**
     * For a given Date (which can be null), return it in string format-
     *
     * @param date the given Date.
     * @return an empty String if the given date is null; otherwise the date as a String in the format dd-MM-yyyy.
     */
    private String getStringFromDate(Date date) {
        return (date != null) ? simpleDateFormat.format(date) : "";
    }

    /**
     * Add the listeners (event handlers) to the input fields (text fields, and the image view).
     */
    private void addListeners() {
        //for the fields add their event listeners (eg iamgeview add to hbox of label)
        addImageView.setOnMouseClicked(new EventHandler<MouseEvent>() {
            @Override
            public void handle(MouseEvent event) {
                System.out.println("Add subject in textfield to subject list: " + subjectTextField.getText());
                addTextToSubjects(subjectTextField.getText().trim());
                subjectTextField.setText("");
            }
        });
        subjectTextField.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {
                System.out.println("Enter is pressed, add subject to list: " + subjectTextField.getText());
                addTextToSubjects(subjectTextField.getText().trim());
                subjectTextField.setText("");
            }
        });
        firstDateTextField.focusedProperty().addListener(new ChangeListener<Boolean>() {
            @Override
            public void changed(ObservableValue<? extends Boolean> observable, Boolean oldValue, Boolean newValue) {
                //validate date before setting it
                if (isValidDateInput(firstDateTextField.getText())) {
                    firstDateTextField.pseudoClassStateChanged(errorClass, false);
                    result.getTimelineDate().setDate1(getDate(firstDateTextField.getText().trim()));
                    textFieldStates.set(0, TextFieldState.CORRECT);

                    enableSave();
                } else {
                    firstDateTextField.pseudoClassStateChanged(errorClass, true);
                    textFieldStates.set(0, TextFieldState.WRONG);
                    disableSave();
                }
            }
        });
        secondDateTextField.focusedProperty().addListener(new ChangeListener<Boolean>() {
            @Override
            public void changed(ObservableValue<? extends Boolean> observable, Boolean oldValue, Boolean newValue) {
                //validate date before setting it
                if (secondDateTextField.getText().trim().length() == 0) {
                    //no second date
                    result.getTimelineDate().setDate2(null);
                } else if (isValidDateInput(secondDateTextField.getText()) && getDate(secondDateTextField.getText().trim()).compareTo(result.getTimelineDate().getDate1()) > 0) {
                    //valid input
                    secondDateTextField.pseudoClassStateChanged(errorClass, false);
                    result.getTimelineDate().setDate2(getDate(secondDateTextField.getText().trim()));
                    textFieldStates.set(1, TextFieldState.CORRECT);
                    enableSave();
                } else {
                    secondDateTextField.pseudoClassStateChanged(errorClass, true);//disable button (need observer of this to tell)
                    textFieldStates.set(1, TextFieldState.WRONG);
                    disableSave();
                }
            }
        });
        eventTextArea.focusedProperty().addListener(new ChangeListener<Boolean>() {
            @Override
            public void changed(ObservableValue<? extends Boolean> observable, Boolean oldValue, Boolean newValue) {
                //save the event once we leave it
                System.out.println("Saving result event: " + eventTextArea.getText());
                result.setEvent(eventTextArea.getText());
            }
        });
    }

    /**
     * For the given String (which should be a Subject), add it to Subjects of the Result (and update the list of
     * Subjects that is being shown).
     *
     * @param newSubject the given String.
     */
    private void addTextToSubjects(String newSubject) {
        if (newSubject.length() >= 1) {
            result.addSubject(newSubject);
            List<Label> labels = getSubjectLabels(result.getSubjects());
            subjectsHBox.getChildren().setAll(labels);
        }
    }

    /**
     * For the given String, produce a Date (assuming the String is in the format: dd-MM-yyyy), else null will be
     * returned.
     *
     * @param date the given String.
     * @return a non-null Date object if the given String is of the format: dd-MM-yyyy; otherwise null.
     */
    private Date getDate(String date) {
        Date toReturn = null;
        try {
            toReturn = validInputFormat.parse(date);
        } catch (ParseException e) {
            e.printStackTrace();
        }
        return toReturn;
    }

    /**
     * Called to disable the save button in the Dialog.
     */
    private void disableSave() {
        //tell the dialog to disable save
        editEventDialogObserver.disableSave(true);
    }

    /**
     * Called to enable the save button in the Dialog.
     */
    private void enableSave() {
        boolean canSave = true;
        for (TextFieldState textFieldState : textFieldStates) {
            if (textFieldState == TextFieldState.WRONG) {
                canSave = false;
                break;
            }
        }
        if (canSave) {
            System.out.println("Enabling Save");
            //tell the dialog to enable save
            editEventDialogObserver.disableSave(false);
        }
    }

    /**
     * Checks whether the given input is of the valid format (dd-MM-yyyy).
     *
     * @param input a String date
     * @return true if the input is of the format dd-MM-yyyy, false otherwise.
     */
    private boolean isValidDateInput(String input) {
        try {
            validInputFormat.setLenient(false);
            validInputFormat.parse(input);
            return input.length() == 10;
        } catch (ParseException e) {
            return false;
        }
    }

    /**
     * Getter method for the root layout that this Controller is representing, which is a GridPane.
     *
     * @return the GridPane of the layout this controller is representing, which is the root layout.
     */
    public GridPane getRootGridPane() {
        return rootGridPane;
    }

    /**
     * Creates a Confirmation Alert Dialog, to allow the user to choose whether the given Subject String should be
     * removed from the Result. The users response is returned.
     *
     * @param subject the given Subject String.
     * @return true if the YES option is picked in the Dialog (that is the subject should be deleted); false otherwise
     */
    private boolean shouldDeleteSubjectLabel(String subject) {
        Alert confirmationDeleteDialog = new Alert(Alert.AlertType.CONFIRMATION);
        confirmationDeleteDialog.setTitle("Deleting a Subject");
        confirmationDeleteDialog.setContentText("Are you sure you want to delete: " + subject + " from the subjects of this event?");
        confirmationDeleteDialog.getButtonTypes().setAll(ButtonType.YES, ButtonType.NO, ButtonType.CANCEL);

        Optional<ButtonType> response = confirmationDeleteDialog.showAndWait();
        return response.get() == ButtonType.YES;
    }
}
