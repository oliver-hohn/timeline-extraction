package frontend.controllers;

import backend.process.FileData;
import backend.process.ProcessFiles;
import backend.process.Result;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.layout.BorderPane;

import java.io.File;
import java.io.IOException;

/**
 * Created by Oliver on 26/01/2017.
 */
public class DocumentReaderController{
    @FXML
    private TextArea documentTextArea;
    @FXML
    private Label documentLabel;
    @FXML
    private BorderPane rootBorderPane;

    public DocumentReaderController(Result result){
        FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("res/documentReader.fxml"));
        fxmlLoader.setController(this);
        try {
            fxmlLoader.load();
        } catch (IOException e) {
            e.printStackTrace();
        }
        System.out.println("documentTextArea: "+documentTextArea);
        System.out.println("documentLabel: "+ documentLabel);
        setData(result);

    }


    private void setData(Result result){
        documentLabel.setText(result.getFileData().getFileName());
        String stringInFile;
        if((stringInFile =getStringInFile(result.getFileData())) != null){
            documentTextArea.setText(stringInFile);
        }else{
            //TODO: show alert that tells file is unavailable, then closes reader
            System.out.println("File is unavailable, cant be read");
            documentTextArea.setText("File Is Unavailable. It could have been deleted, or it's permissions have changed");
        }
    }

    private void highlightText(String textToHighlight, String from){
        int startHighlight = from.indexOf(textToHighlight);
        int endHighlight = startHighlight + textToHighlight.length();//as we highlight the original sentence, which we know its length, and its start index
        //TODO: richtextfx
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

    public BorderPane getRootBorderPane(){
        return rootBorderPane;
    }
}
