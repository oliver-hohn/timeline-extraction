package frontend.observers;

import backend.process.Result;

/**
 * Implemented by the Observer of this TimelineRow (that is the holder of the ListView), so that the cells can communicate
 * with the overall listView.
 */
public interface TimelineRowObserver {
    /**
     * Called by a TimelineRow to inform the Observer (holder of the ListView), that the data it is holding has been
     * updated.
     *
     * @param updatedResult the updated data of this TimelineRow.
     * @param position      the position this cell had in the ListView.
     */
    void update(Result updatedResult, int position);

    /**
     * Called by a TimelineRow to inform the Observer (holder of the ListView), that this event (in the given position),
     * needs to be deleted from the list.
     *
     * @param position the position of the event to be deleted.
     */
    void delete(int position);
}
