package frontend;

import backend.process.Result;
import frontend.controllers.EditEventController;
import frontend.observers.EditEventDialogObserver;
import javafx.scene.Node;
import javafx.scene.control.ButtonBar;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Dialog;
import javafx.util.Callback;

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
     */
    public static Dialog<Result> getEditEventDialog(Result result, int position) {
        //make a copy of the passed in Result object, use it to change values, and pass that to return.
        Result copyResult = result.copyOfThis();
        Dialog<Result> dialog = new Dialog<>();
        dialog.setTitle("Event #" + position);
        ButtonType buttonTypeSave = new ButtonType("Save", ButtonBar.ButtonData.OK_DONE);
        ButtonType buttonTypeCancel = new ButtonType("Cancel", ButtonBar.ButtonData.CANCEL_CLOSE);
        dialog.getDialogPane().getButtonTypes().addAll(buttonTypeSave, buttonTypeCancel);


        dialog.getDialogPane().setContent(new EditEventController(copyResult, new EditEventDialogObserver() {
            @Override
            public void disableSave(boolean disableSave) {
                Node saveButton = dialog.getDialogPane().lookupButton(buttonTypeSave);
                saveButton.setDisable(disableSave);
            }
        }).getRootGridPane());
        dialog.setResultConverter(new Callback<ButtonType, Result>() {
            @Override
            public Result call(ButtonType param) {
                if (param == buttonTypeSave) {
                    return copyResult;
                }
                return null;
            }
        });
        return dialog;
    }
}
