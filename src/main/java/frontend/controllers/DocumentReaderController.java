package frontend.controllers;

import backend.process.FileData;
import backend.process.ProcessFiles;
import backend.process.Result;
import frontend.observers.DocumentReaderObserver;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.MenuItem;
import javafx.scene.layout.BorderPane;
import org.fxmisc.richtext.InlineCssTextArea;

import java.awt.*;
import java.awt.datatransfer.StringSelection;
import java.io.File;
import java.io.IOException;

/**
 * Controller for the layout of the Document Reader
 */
public class DocumentReaderController {
    @FXML
    private InlineCssTextArea documentInlineCssTextArea;
    @FXML
    private MenuItem closeMenuItem;
    @FXML
    private MenuItem copyMenuItem;
    @FXML
    private BorderPane rootBorderPane;
    private DocumentReaderObserver documentReaderObserver;

    /**
     * Called to create the layout for the Document Reader. It creates a text area with the text of the File where the
     * given Result originates from, and it highlights the specific sentence that produced the given Result.
     * The Observer is used to inform the creator of the window that uses this layout, to inform them when the Close
     * menu item was pressed (to close the window).
     *
     * @param result                 the given Result.
     * @param documentReaderObserver the Observer that holds this layout, to inform when to close the window.
     */
    public DocumentReaderController(Result result, DocumentReaderObserver documentReaderObserver) {
        this.documentReaderObserver = documentReaderObserver;
        FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("documentReader.fxml"));
        fxmlLoader.setController(this);
        try {
            fxmlLoader.load();
        } catch (IOException e) {
            e.printStackTrace();
        }
        setData(result);
        setOnActionListeners();
    }


    /**
     * Called to set the data displayed in the layout, which in this case is the text of the File set in the text area
     * with the relevant sentence that produced the given result highlighted.
     *
     * @param result the given Result (used to determine the File and sentence that produced this Result).
     */
    private void setData(Result result) {
        String stringInFile;
        if ((stringInFile = getStringInFile(result.getFileData())) != null) {
            documentInlineCssTextArea.clear();
            documentInlineCssTextArea.replaceText(stringInFile);
            ContextMenu contextMenu = new ContextMenu();
            MenuItem menuItemCopy = new MenuItem("Copy");
            menuItemCopy.setDisable(true);//initially cant copy as no text is selected
            copyMenuItem.setDisable(true);
            menuItemCopy.setOnAction(new EventHandler<ActionEvent>() {
                @Override
                public void handle(ActionEvent event) {
                    copy();//copy the selected text
                }
            });
            contextMenu.getItems().setAll(menuItemCopy);
            documentInlineCssTextArea.selectedTextProperty().addListener(new ChangeListener<String>() {
                @Override
                public void changed(ObservableValue<? extends String> observable, String oldValue, String newValue) {
                    if (newValue.equals("")) {
                        menuItemCopy.setDisable(true);
                        copyMenuItem.setDisable(true);
                    } else {
                        menuItemCopy.setDisable(false);
                        copyMenuItem.setDisable(false);
                    }
                }
            });
            documentInlineCssTextArea.setContextMenu(contextMenu);
            highlightText(result.getOriginalString(), stringInFile);
        } else {
            System.out.println("File is unavailable, cant be read");
            documentInlineCssTextArea.replaceText("");
            Alert documentUnvailable = documentUnavailableDialog(result.getFileData());
            documentUnvailable.showAndWait();
            //should close this window, as the file is unavailable
            rootBorderPane = null;
        }
    }

    /**
     * For the given FileData (holds the Files name, and represents it), produce an Alert Dialog to show to the User,
     * to inform them that the File which they wish to read is unavailable.
     *
     * @param fileData the given FileData
     * @return the Alert Dialog the informs the User the File is unavailable to read.
     */
    private Alert documentUnavailableDialog(FileData fileData) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Document Unavailable");
        alert.setHeaderText(null);
        alert.setContentText("The File: " + fileData.getFileName() + " is Unavailable");
        alert.getDialogPane().getButtonTypes().setAll(ButtonType.OK);
        return alert;
    }

    /**
     * Called to set the action listeners for the Menu Items: Close and Copy
     */
    private void setOnActionListeners() {
        copyMenuItem.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {
                copy();
            }
        });
        closeMenuItem.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {
                close();
            }
        });
    }

    /**
     * Called to highlight the text "textToHighlight" in the "from" text, in the text area of this layout.
     *
     * @param textToHighlight the given text that needs to be highlighted. Part of the "from" text
     * @param from            the full text, that contains the text that needs to be highlighted.
     */
    private void highlightText(String textToHighlight, String from) {//highlight first occurrence of the text to highlight
        int startHighlight = from.indexOf(textToHighlight);
        int endHighlight = startHighlight + textToHighlight.length();//as we highlight the original sentence, which we know its length, and its start index
        if (endHighlight > from.length()) {//if we are over the limit of the text, then we go up to that point
            endHighlight = documentInlineCssTextArea.getText().length();
        }
        System.out.println("EndHighlight: " + endHighlight);
        if (startHighlight >= 0 && startHighlight < from.length() && endHighlight >= 0) {
            documentInlineCssTextArea.setStyle(startHighlight, endHighlight, "-fx-fill: blue; -fx-font-weight: bold");
        }
    }

    /**
     * Called to extract the text in the File which the given FileData represents.
     *
     * @param fileData the FileData that represents the File from which we are extracting text from.
     * @return null, if the File has been moved/deleted or we don't have read rights; otherwise the text of the File is
     * returned.
     */
    private String getStringInFile(FileData fileData) {
        if (fileData != null) {
            File file = new File(fileData.getFilePath());
            if (file.exists() && file.isFile() && file.canRead()) {
                ProcessFiles processFiles = new ProcessFiles();
                return processFiles.getTextInFile(file);
            }
        }
        return null;
    }

    /**
     * Called when the Close Menu Item is pressed. Inform the observer that they need to close the window in which this
     * layout resides in.
     */
    private void close() {
        System.out.println("Close Window");
        if (documentReaderObserver != null) {
            documentReaderObserver.close();
        }
    }

    /**
     * Called when the Copy Menu Item is pressed. Used to copy the text in the file to the clipboard.
     */
    private void copy() {
        if (documentInlineCssTextArea != null) {
            System.out.println("Copy Text to Clipboard: " + documentInlineCssTextArea.getSelectedText());
            Toolkit toolkit = Toolkit.getDefaultToolkit();
            if (toolkit != null && documentInlineCssTextArea.getSelectedText() != null) {
                toolkit.getSystemClipboard().setContents(new StringSelection(documentInlineCssTextArea.getSelectedText()), null);
            }
        }
    }

    /**
     * Get the BorderPane layout of the layout produced when a new object is made (that was populated with the given
     * Result object). This is the root layout.
     *
     * @return the BorderPane (root) layout, produced when the constructor is invoked.
     */
    public BorderPane getRootBorderPane() {
        return rootBorderPane;
    }
}
