package backend.process;

import java.util.ArrayList;

/**
 * Interface to be implemented, to inform the Listener of ProcessFile Thread of when a file has been processed.
 */
public interface ProcessFileCallback {
    /**
     * Inform the Listener that the passed in File to ProcessFile has been processed, and pass the Results of processing
     * the given File.
     *
     * @param results the Results from processing the given File passed into the ProcessFile Thread.
     */
    void callBack(ArrayList<Result> results);
}
