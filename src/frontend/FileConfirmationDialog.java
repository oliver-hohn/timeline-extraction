package frontend;

import backend.process.FileData;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.css.PseudoClass;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Priority;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;

/**
 * Used to create and the confirmation dialog that will contain the filenames, and editable base dates for the File.
 */
public class FileConfirmationDialog {
    private final PseudoClass errorClass = PseudoClass.getPseudoClass("error");
    private final SimpleDateFormat validInputFormat = new SimpleDateFormat("dd-MM-yyyy");
    private ArrayList<TextFieldState> textFieldStates;

    /**
     * For the List of FileData, produce an Alert Confirmation Dialog, where the filename and file creation date are
     * shown, so that the user can determine whether or not to use it as a base date.
     *
     * @param fileDatas the List of FileData for which we need to create the list of labels and text fields in the dialog.
     * @return an Alert object that can be shown for the user to confirm to use these dates for the given files.
     */
    public Alert getConfirmationFileDialog(ArrayList<FileData> fileDatas) {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Confirm the Base Dates");
        alert.setContentText("The following Base Dates will be used for the given Files. Change them appropriately");

        ScrollPane scrollPane = new ScrollPane();
        scrollPane.setPadding(new Insets(10));
        scrollPane.setFitToWidth(true);//its content should resize to fit the width of the scroll pane (if it gets resized)
        GridPane fileDateGridPane = new GridPane();
        fileDateGridPane.setMaxWidth(Double.MAX_VALUE);
        fileDateGridPane.setPadding(new Insets(10));
        fileDateGridPane.setHgap(10);//add spacing between the cells in the gridpane
        fileDateGridPane.setVgap(10);
        scrollPane.setContent(fileDateGridPane);
        textFieldStates = new ArrayList<>(fileDatas.size());
        for (int i = 0; i < fileDatas.size(); i++) {
            FileData fileData = fileDatas.get(i);
            Label fileNameLabel = new Label(fileData.getFileName());
            TextField dateTextField = new TextField(fileData.getCreationDateFormattedDayMonthYear());
            dateTextField.getStylesheets().add(getClass().getResource("controllers/res/customTextField.css").toExternalForm());
            dateTextField.setMinWidth(150);//150 pixels is enough for 12 characters
            dateTextField.setMaxHeight(35);//enough for one line, if text size is 15
            dateTextField.setAlignment(Pos.CENTER);
            textFieldStates.add(i, TextFieldState.CORRECT);
            GridPane.setVgrow(dateTextField, Priority.NEVER);//1 line always
            GridPane.setHgrow(dateTextField, Priority.ALWAYS);
            int finalI = i;//to input in the array list at this index
            dateTextField.focusedProperty().addListener(new ChangeListener<Boolean>() {
                @Override
                public void changed(ObservableValue<? extends Boolean> observable, Boolean oldValue, Boolean newValue) {
                    if (!newValue) {
                        //check validate text field input
                        String input = dateTextField.getText().trim();
                        System.out.println("Inputted: " + input);
                        if (isValidDateInput(input)) {
                            dateTextField.pseudoClassStateChanged(errorClass, false);
                            fileData.setCreationDate(input);
                            textFieldStates.set(finalI, TextFieldState.CORRECT);
                            if (isAllCorrect(textFieldStates)) {
                                alert.getDialogPane().lookupButton(ButtonType.OK).setDisable(false);
                            }
                        } else {
                            dateTextField.pseudoClassStateChanged(errorClass, true);
                            textFieldStates.set(finalI, TextFieldState.WRONG);
                            alert.getDialogPane().lookupButton(ButtonType.OK).setDisable(true);
                        }
                    }
                }
            });

            fileDateGridPane.add(fileNameLabel, 0, i);
            fileDateGridPane.add(dateTextField, 1, i);
        }
        alert.getDialogPane().setExpandableContent(scrollPane);
        return alert;
    }

    /**
     * Whether or not all the states in the given list of TextFieldStates are CORRECT (true) or not (false).
     *
     * @param textFieldStates a list of TextFieldStates to check
     * @return true if all TextFieldStates in the given input are CORRECT, false otherwise.
     */
    private boolean isAllCorrect(ArrayList<TextFieldState> textFieldStates) {
        boolean toReturn = true;
        for (TextFieldState textFieldState : textFieldStates) {
            if (textFieldState == TextFieldState.WRONG) {
                toReturn = false;
                break;
            }
        }
        System.out.println("Checked: " + textFieldStates + " returned: " + toReturn);
        return toReturn;
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
            return true;
        } catch (ParseException e) {
            return false;
        }
    }
}
