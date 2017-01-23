package frontend;

import backend.process.Result;
import frontend.controllers.EditEventController;
import javafx.scene.control.ButtonBar;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Dialog;

/**
 * Created by Oliver on 23/01/2017.
 */
public class EditEventDialog {
    public Dialog<Boolean> getEditEventDialog(Result result, int position){
        Dialog<Boolean> dialog = new Dialog<>();
        dialog.setTitle("Event #"+position);
        ButtonType buttonTypeSave = new ButtonType("Save", ButtonBar.ButtonData.APPLY);
        ButtonType buttonTypeCancel = new ButtonType("Cancel", ButtonBar.ButtonData.CANCEL_CLOSE);
        dialog.getDialogPane().getButtonTypes().addAll(buttonTypeSave, buttonTypeCancel);

        dialog.getDialogPane().setContent(new EditEventController(result).getRootGridPane());
        return dialog;
    }
}
