package frontend.observers;

/**
 * Implemented by the Observer of the EditEventDialog Content. To inform, from the content of the Dialog, to the holder
 * of the Dialog to disable/enable the save button.
 */
public interface EditEventDialogObserver {
    /**
     * Called when the Dialog Save button should be disabled or enabled.
     */
    void disableSave(boolean disableSave);
}
