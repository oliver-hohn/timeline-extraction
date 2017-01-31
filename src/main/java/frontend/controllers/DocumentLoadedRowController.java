package frontend.controllers;

import backend.process.FileData;
import frontend.observers.DocumentsLoadedObserver;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.Label;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.GridPane;

import java.io.IOException;

/**
 * Controller for each row in the Documents Loaded listview.
 */
public class DocumentLoadedRowController {
    @FXML
    private ImageView removingImageView;
    @FXML
    private Label documentLabel;
    @FXML
    private GridPane gridPane;
    private DocumentsLoadedObserver documentsLoadedObserver;

    /**
     * Creates a Controller and loads the layout for a row in the Loaded Documents listview. Observer is informed when
     * that file needs to be removed.
     *
     * @param fileData                FileData for which this row is made for.
     * @param documentsLoadedObserver observer that gets notified when the Results for the given FileData need to be removed.
     */
    public DocumentLoadedRowController(FileData fileData, DocumentsLoadedObserver documentsLoadedObserver) {
        this.documentsLoadedObserver = documentsLoadedObserver;
        FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("documentLoadedRow.fxml"));
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
                System.out.println("Remove this Document: " + fileData);
                documentsLoadedObserver.remove(fileData);
            }
        });
    }

    /**
     * For the label that is supposed to show the File name, set it.
     *
     * @param fileData label for which we set the File name.
     */
    private void setData(FileData fileData) {
        documentLabel.setText(fileData.getFileName());
    }

    /**
     * Get the root layout for this row (i.e. its GridPane, under which everything is set).
     *
     * @return the root GridPane layout for this row.
     */
    public GridPane getGridPane() {
        return gridPane;
    }
}
