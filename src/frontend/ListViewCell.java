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
            TimelineRowController timelineRowController = new TimelineRowController();
            timelineRowController.setData(result);
            setGraphic(timelineRowController.getGroup());
/*            Cell cell = new Cell();
            //set the info for the cell
            cell.setInfo(result);
            setGraphic(cell.getvBox());*/
        }
    }
}
