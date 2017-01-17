package frontend;

import backend.process.FileData;
import frontend.observers.DocumentsLoadedObserver;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Bounds;
import javafx.scene.control.Label;
import javafx.scene.effect.ColorInput;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.GridPane;
import javafx.scene.paint.Color;

import java.io.IOException;

/**
 * Created by Oliver on 17/01/2017.
 */
public class DocumentLoadedRowController {
    @FXML
    private ImageView removingImageView;
    @FXML
    private Label documentLabel;
    @FXML
    private GridPane gridPane;
    private DocumentsLoadedObserver documentsLoadedObserver;
    public DocumentLoadedRowController(FileData fileData, DocumentsLoadedObserver documentsLoadedObserver){
        this.documentsLoadedObserver = documentsLoadedObserver;
        FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("res/documentLoadedRow.fxml"));
        fxmlLoader.setController(this);
        try {
            fxmlLoader.load();
        } catch (IOException e) {
            e.printStackTrace();
        }
        setData(fileData);
        removingImageView.setOnMouseClicked(new EventHandler<MouseEvent>() {
            @Override
            public void handle(MouseEvent event) {
                System.out.println("Remove this Document: "+fileData);
                documentsLoadedObserver.remove(fileData);
            }
        });
    }

    private void setData(FileData fileData){
        documentLabel.setText(fileData.getFileName());
    }

    public GridPane getGridPane(){
        return gridPane;
    }
}
