package frontend;

import backend.process.Result;
import javafx.scene.control.ListCell;

/**
 * Is used to create the layout for each cell (row) in the listview (timeline).
 */
public class ListViewCell extends ListCell<Result> {
    /**
     * Called whenever a row needs to be shown/created on the screen.
     *
     * @param result the Result object for which this row has to display data for.
     * @param empty  whether or not the Row is empty (i.e. result == null).
     */
    @Override
    protected void updateItem(Result result, boolean empty) {
        super.updateItem(result, empty);
        if (result != null) {
            System.out.println("Got Item: " + result);
            System.out.println("Position: " + getIndex());
            TimelineRowController timelineRowController = new TimelineRowController(getIndex());
            timelineRowController.setData(result);
            setGraphic(timelineRowController.getGroup());
        }
    }
}
