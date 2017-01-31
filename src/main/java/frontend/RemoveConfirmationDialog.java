package frontend;

import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;

/**
 * Used to produce a Confirmation Dialog to ask whether the events for a specified File should be removed from the
 * Timeline.
 */
public class RemoveConfirmationDialog {
    /**
     * Produces a Dialog to allow the User to confirm whether or not the events related to the selected File should be
     * removed or not.
     *
     * @param fileName the name of the File which links to the events to be removed.
     * @return a Confirmation Alert Dialog with options to remove or not the events linked to the given File name.
     */
    public static Alert getRemoveConfirmationDialog(String fileName) {
        Alert removeConfirmationDialog = new Alert(Alert.AlertType.CONFIRMATION);
        removeConfirmationDialog.setTitle("Removing File");
        removeConfirmationDialog.setContentText("Are you sure you want to remove the events for " + fileName + "?");
        removeConfirmationDialog.getButtonTypes().setAll(ButtonType.YES, ButtonType.NO, ButtonType.CANCEL);
        return removeConfirmationDialog;
    }
}
