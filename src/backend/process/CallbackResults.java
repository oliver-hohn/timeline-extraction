package backend.process;

import java.util.ArrayList;

/**
 * Interface for classes that call backend.process.ProcessFiles, so that when it finishes processing all Files passed in, the class (listener)
 * that called it can be informed.
 */
public interface CallbackResults {
    /**
     * Inform the Listener that all Files have been processed, and return the result of processing the Files.
     *
     * @param results the backend.process.Result objects obtained from processing all Files.
     */
    void gotResults(ArrayList<Result> results);
}
