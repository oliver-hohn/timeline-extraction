package frontend.dialogs;

import backend.process.Result;
import frontend.controllers.EditEventController;
import frontend.observers.EditEventDialogObserver;
import javafx.event.EventHandler;
import javafx.scene.Node;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonBar;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Dialog;
import javafx.scene.input.MouseEvent;
import javafx.util.Callback;

import java.util.Optional;

/**
 * Class that creates an EditEvent Dialog and returns it.
 */
public class EditEventDialog {
    /**
     * Called to produce a edit Dialog for the given Result object. Any changes made do not change the given Result
     * object (as it is cloned).
     *
     * @param result   the Result object for which this Edit Dialog needs to be produced (i.e. to populate the fields).
     * @param position the position the Result is in the timeline (to show at the top of the Edit Dialog)
     * @return a Dialog  where if the user decides to Save the changes made then its result will be a new Result object with the changes made by the
     * user (which can be the same as the given Result object if no changes are made, or a new Result object with the data of the previous plus the changes made).
     * This Result object is encapsulated in a DialogResult, that holds the option selected by the user (SAVE, DELETE, CANCEL),
     * to determine what to do with the copy Result object and the original Result object in the ListView.
     */
    public static Dialog<DialogResult> getEditEventDialog(Result result, int position) {
        //make a copy of the passed in Result object, use it to change values, and pass that to return.
        Dialog<DialogResult> dialog = new Dialog<>();
        try {
            Result copyResult = (Result) result.clone();
            DialogResult dialogResult = new DialogResult(copyResult);
            dialog.setTitle("Editing Event");
            dialog.setHeaderText("Event #" + position);
            ButtonType buttonTypeSave = new ButtonType("Save", ButtonBar.ButtonData.OK_DONE);
            ButtonType buttonTypeCancel = new ButtonType("Cancel", ButtonBar.ButtonData.CANCEL_CLOSE);
            ButtonType buttonTypeDelete = new ButtonType("Delete", ButtonBar.ButtonData.LEFT);

            dialog.getDialogPane().getButtonTypes().addAll(buttonTypeDelete, buttonTypeSave, buttonTypeCancel);
            Node deleteButton = dialog.getDialogPane().lookupButton(buttonTypeDelete);
            deleteButton.setOnMousePressed(new EventHandler<MouseEvent>() {
                @Override
                public void handle(MouseEvent event) {
                    System.out.println("Delete Pressed");
                    //show dialog to delete event, to confirm etc, tell listener they need to delete this event
                    Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
                    alert.setTitle("Delete");
                    alert.setHeaderText(null);
                    alert.setContentText("Are you sure you want to delete?");
                    alert.getButtonTypes().setAll(ButtonType.YES, ButtonType.NO);
                    Optional<ButtonType> result = alert.showAndWait();
                    if (result.isPresent() && result.get() == ButtonType.YES) {
                        //delete event
                        dialogResult.setResultType(DialogResult.ResultType.DELETE);
                        //then stop showing
                        if (dialog.isShowing()) {
                            dialog.close();
                        }
                    }
                }
            });
            dialog.getDialogPane().setContent(new EditEventController(copyResult, new EditEventDialogObserver() {
                @Override
                public void disableSave(boolean disableSave) {
                    Node saveButton = dialog.getDialogPane().lookupButton(buttonTypeSave);
                    saveButton.setDisable(disableSave);
                }
            }).getRootGridPane());
            dialog.setResultConverter(new Callback<ButtonType, DialogResult>() {
                @Override
                public DialogResult call(ButtonType param) {
                    if (param.equals(buttonTypeSave)) {
                        dialogResult.setResultType(DialogResult.ResultType.SAVE);
                    }
                    return dialogResult;
                }
            });
        } catch (CloneNotSupportedException e) {
            e.printStackTrace();
        }
        return dialog;
    }

    /**
     * Class that holds the option selected by the user in the Edit Event Dialog, and the Result object with the changes
     * made it to it through the input fields in the Dialog.
     */
    public static class DialogResult {
        /**
         * The different options the user has available in the Edit Event Dialog.
         */
        public enum ResultType {
            SAVE, CANCEL, DELETE
        }

        private Result result;
        private ResultType resultType;

        /**
         * To create a new DialogResult object.
         *
         * @param result the copy of the Result object that the user will modify with in the input fields.
         */
        DialogResult(Result result) {
            this.result = result;
            this.resultType = ResultType.CANCEL;
        }

        /**
         * Getter for the copy of the Result object that the user modified with the input fields.
         *
         * @return the copy of the Result object.
         */
        public Result getResult() {
            return result;
        }

        /**
         * Getter for the option selected by the User when they leave the Edit Event Dialog.
         *
         * @return the option selected.
         */
        public ResultType getResultType() {
            return resultType;
        }

        /**
         * Setter for the option selected by the User when they leave the Edit Event Dialog.
         *
         * @param resultType the option selected.
         */
        void setResultType(ResultType resultType) {
            this.resultType = resultType;
        }
    }
}
