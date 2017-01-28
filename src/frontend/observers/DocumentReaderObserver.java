package frontend.observers;

/**
 * Interface to be implemented by the Observer of the DocumentReaderController.
 */
public interface DocumentReaderObserver {
    /**
     * Called to allow the Controller to inform the Observer when they need to close the window in which the layout
     * of the Controller resides.
     */
    void close();
}
