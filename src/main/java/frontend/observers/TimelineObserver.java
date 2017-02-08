package frontend.observers;

import backend.process.Result;

import java.util.List;

/**
 * Interface for the Observer of the Timeline scene.
 */
public interface TimelineObserver extends MenuBarObserver {
    /**
     * Called when the "Load Documents" button is pressed.
     */
    void loadDocuments();

    /**
     * Called when the "Save to ..." button is pressed.
     * @param resultList the List of Results to save.
     */
    void saveTo(List<Result> resultList);
}
