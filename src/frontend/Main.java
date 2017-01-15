package frontend;

import backend.process.CallbackResults;
import backend.process.FileData;
import backend.process.ProcessFiles;
import backend.process.Result;
import backend.system.BackEndSystem;
import frontend.controllers.ListViewController;
import frontend.controllers.StartUpController;
import frontend.observers.StartUpObserver;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.concurrent.Task;
import javafx.concurrent.WorkerStateEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.FileChooser;
import javafx.stage.Stage;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * Main class that is used to run the program (i.e. show the UI that uses the backend).
 */
public class Main extends Application implements StartUpObserver, CallbackResults {
    private final static String TAG = "MAIN: ";
    private Stage primaryStage;
    private ArrayList<FileData> fileDataList = new ArrayList<>();//add/remove to this, holds the information of the Files for which we are showing results to
    private ArrayList<Result> currentResults = new ArrayList<>();//list of Results that it is currently showing

    @Override
    public void start(Stage primaryStage) throws Exception {
        //need to start engine
        BackEndSystem.getInstance();//thread waits for this to be done
        System.out.println("Called getInstance");
        FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("res/startup.fxml"));
        primaryStage.setScene(new Scene(fxmlLoader.load(), 800, 600));
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
        Task<Boolean> processFileTask = new Task<Boolean>() {//to run the processing of files on a separate thread, and show a loading dialog
            @Override
            protected Boolean call() throws Exception {
                ProcessFiles processFiles = new ProcessFiles();
                processFiles.processFiles(files, Main.this);
                return null;
            }
        };

        processFileTask.setOnRunning(new EventHandler<WorkerStateEvent>() {
            @Override
            public void handle(WorkerStateEvent event) {
                //Show loading dialog
                System.out.println(TAG+"Working");
            }
        });

        processFileTask.setOnSucceeded(new EventHandler<WorkerStateEvent>() {
            @Override
            public void handle(WorkerStateEvent event) {//hasn't finished, just finished starting to run the last thread
                System.out.println(TAG+"Finished");
            }
        });

        if(files != null) {
            new Thread(processFileTask).start();
        }
    }

    @Override
    public void showAbout() {
        System.out.println(TAG+"show about information");
    }

    @Override
    public void close() {
        System.out.println(TAG+"close program");
        primaryStage.close();
    }

    @Override
    public void timeline() {
        System.out.println(TAG+"timeline options");
    }

    @Override
    public synchronized void gotResults(ArrayList<Result> results, ArrayList<FileData> fileDataList) {
        //stop showing the loading dialog
        System.out.println(TAG+"Processed Files");
        for(Result result: results){
            System.out.println(result);
        }
        System.out.println(TAG+"List of data of Files");
        for (FileData fileData: fileDataList){
            System.out.println(fileData);
        }

        this.fileDataList.addAll(fileDataList);//TODO: compare if the FileData is already there
        this.currentResults.addAll(results);//add the results of processing these files to the list of current results
        //TODO: process the Files to produce the scene and swap (ie start another thread, show wait dialog here, when finish swap scene)
        //needs to be done on ui thread
        Platform.runLater(new Runnable() {
            @Override
            public void run() {
                System.out.println("About to show list");
                FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("res/listView.fxml"));
                try {
                    primaryStage.setScene(new Scene(fxmlLoader.load(), 800, 600));
                    primaryStage.setTitle("Automated Timeline Extractor - Oliver Philip Höhn");
                    ListViewController listViewController = fxmlLoader.getController();
                    listViewController.setListView(results);
                    primaryStage.show();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        });

    }



}