package frontend.controllers;

import backend.process.FileData;
import backend.process.ProcessFiles;
import backend.process.Result;
import frontend.observers.DocumentReaderObserver;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.MenuItem;
import javafx.scene.layout.BorderPane;
import org.fxmisc.richtext.InlineCssTextArea;

import java.awt.*;
import java.awt.datatransfer.StringSelection;
import java.io.File;
import java.io.IOException;

/**
 * Created by Oliver on 26/01/2017.
 */
public class DocumentReaderController{
    @FXML
    private InlineCssTextArea documentInlineCssTextArea;
    @FXML
    private MenuItem closeMenuItem;
    @FXML
    private MenuItem copyMenuItem;
    @FXML
    private BorderPane rootBorderPane;
    private DocumentReaderObserver documentReaderObserver;

    public DocumentReaderController(Result result, DocumentReaderObserver documentReaderObserver){
        this.documentReaderObserver = documentReaderObserver;
        FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("res/documentReader.fxml"));
        fxmlLoader.setController(this);
        try {
            fxmlLoader.load();
        } catch (IOException e) {
            e.printStackTrace();
        }
        System.out.println("documentCodeArea: "+ documentInlineCssTextArea);
        System.out.println("copyMenuItem: "+ copyMenuItem);
        System.out.println("closeMenuItem: "+ closeMenuItem);
        setData(result);

    }


    private void setData(Result result){
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
        String stringInFile;
        if((stringInFile = getStringInFile(result.getFileData())) != null){
            documentInlineCssTextArea.clear();
            documentInlineCssTextArea.replaceText(stringInFile);
            highlightText(result.getOriginalString(), stringInFile);
        }else{
            //TODO: show alert that tells file is unavailable, then closes reader
            System.out.println("File is unavailable, cant be read");
            documentInlineCssTextArea.replaceText("File Is Unavailable. It could have been deleted, or it's permissions have changed");
        }
    }

    private void highlightText(String textToHighlight, String from){//highlight first occurrence of the text to highlight
        int startHighlight = from.indexOf(textToHighlight);
        int endHighlight = startHighlight + textToHighlight.length();//as we highlight the original sentence, which we know its length, and its start index
        System.out.println("Start: "+startHighlight+" End: "+endHighlight );
        System.out.println("From: "+from+" Length: "+from.length());
        System.out.println("Text: "+textToHighlight+" Length: "+textToHighlight.length());
        System.out.println("StartPoint: "+from.substring(startHighlight));
        System.out.println("Length of From: "+documentInlineCssTextArea.getText().length());
        if(endHighlight > from.length()){//if we are over the limit of the text, then we go up to that point
            endHighlight = documentInlineCssTextArea.getText().length();
        }
        System.out.println("EndHighlight: "+endHighlight);
        documentInlineCssTextArea.setStyle(startHighlight, endHighlight, "-fx-fill: red;");
    }

    private String getStringInFile(FileData fileData){
        if(fileData != null) {
            File file = new File(fileData.getFilePath());
            if (file.exists() && file.isFile() && file.canRead()) {
                ProcessFiles processFiles = new ProcessFiles();
                return processFiles.getTextInFile(file);
            }
        }
        return null;
    }

    private void close(){
        System.out.println("Close Window");
        if(documentReaderObserver != null){
            documentReaderObserver.close();
        }
    }

    private void copy(){
        System.out.println("Copy Text to Clipboard");
        Toolkit toolkit = Toolkit.getDefaultToolkit();
        if(toolkit != null && documentInlineCssTextArea.getText() != null){
            toolkit.getSystemClipboard().setContents(new StringSelection(documentInlineCssTextArea.getText()), null);
        }
    }

    public BorderPane getRootBorderPane(){
        return rootBorderPane;
    }
}
