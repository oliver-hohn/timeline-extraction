package frontend;

import backend.process.CallbackResults;
import backend.process.ProcessFiles;
import backend.process.Result;
import backend.system.BackEndSystem;
import frontend.controllers.StartUpController;
import frontend.observers.StartUpObserver;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.layout.Pane;
import javafx.stage.FileChooser;
import javafx.stage.Stage;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

/**
 * Main class that is used to run the program (i.e. show the UI that uses the backend).
 */
public class Main extends Application implements StartUpObserver, CallbackResults {
    private final static String TAG = "MAIN: ";
    private Stage primaryStage;

    @Override
    public void start(Stage primaryStage) throws Exception {
        //need to start engine
        BackEndSystem.getInstance();//thread waits for this to be done
        System.out.println("Called getInstance");
        FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("res/startup.fxml"));
        primaryStage.setScene(new Scene((Pane) fxmlLoader.load(), 600, 400));
        primaryStage.setTitle("Automated Timeline Extractor - Oliver Philip Höhn");
        StartUpController startUpController = fxmlLoader.getController();
        startUpController.setObserver(this);
        primaryStage.show();

        this.primaryStage = primaryStage;
    }

    public static void main(String[] args){
        launch(args);
    }


    @Override
    public void loadFiles() {
        System.out.println(TAG+"need to load files "+Thread.currentThread().getName());
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Open Document Files");
        fileChooser.getExtensionFilters().addAll(new FileChooser.ExtensionFilter("Text Files", "*.txt", "*.pdf", "*.docx"));
        List<File> files = fileChooser.showOpenMultipleDialog(primaryStage);

        //prune list of files, checking extension
        //then processFile
        ProcessFiles processFiles = new ProcessFiles();
        processFiles.processFiles(files, this);
    }

    @Override
    public void showAbout() {
        System.out.println(TAG+"show about information");
    }

    @Override
    public void close() {
        System.out.println(TAG+"close program");
    }

    @Override
    public void timeline() {
        System.out.println(TAG+"timeline options");
    }

    @Override
    public void gotResults(ArrayList<Result> results) {
        System.out.println(TAG+"Processed Files");
        for(Result result: results){
            System.out.println(result);
        }
    }
}
