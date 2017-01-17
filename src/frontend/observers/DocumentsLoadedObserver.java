package frontend.observers;

import backend.process.FileData;

/**
 * Interface for the Observers of the DocumentLoadedRowController.
 */
public interface DocumentsLoadedObserver {
    /**
     * Called when all the Results connected to the given FileData need to be removed. (i.e. removing the File given by
     * FileData from the Timeline).
     * @param fileData the FileData for the File where we want to remove its results from the Timeline.
     */
    void remove(FileData fileData);
}
