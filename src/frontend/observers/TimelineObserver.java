package frontend.observers;

/**
 * Interface for the Observer of the Timeline scene.
 */
public interface TimelineObserver extends MenuBarObserver {
    /**
     * Called when the "Load Documents" button is pressed.
     */
    void loadDocumets();

    /**
     * Called when the "Save to PDF" button is pressed.
     */
    void saveToPDF();
}
