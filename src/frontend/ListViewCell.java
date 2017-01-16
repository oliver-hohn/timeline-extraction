package frontend;

import backend.process.Result;
import javafx.scene.control.ListCell;

/**
 * Created by Oliver on 15/01/2017.
 */
public class ListViewCell extends ListCell {
    @Override
    protected void updateItem(Object item, boolean empty) {
        super.updateItem(item, empty);
        if(item != null){
            Result result = (Result) item;

            System.out.println("Got Item: "+result);
            System.out.println("Position: "+getIndex());
            TimelineRowController timelineRowController = new TimelineRowController(getIndex());
            timelineRowController.setData(result);
            setGraphic(timelineRowController.getGroup());
        }
    }
}
