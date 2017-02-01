package frontend;

import backend.ToPDF;
import backend.process.FileData;
import backend.process.ProcessFiles;
import backend.process.Result;
import backend.system.BackEndSystem;
import edu.stanford.nlp.util.Pair;
import frontend.controllers.ListViewController;
import frontend.controllers.StartUpController;
import frontend.observers.StartUpObserver;
import frontend.observers.TimelineObserver;
import javafx.application.Application;
import javafx.concurrent.Task;
import javafx.concurrent.WorkerStateEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.stage.FileChooser;
import javafx.stage.Stage;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * Main class that is used to run the program (i.e. show the UI that uses the backend).
 */
public class Main extends Application implements StartUpObserver, TimelineObserver {
    private final static String TAG = "MAIN: ";
    private Stage primaryStage;
    private ArrayList<FileData> fileDataList = new ArrayList<>();//add/remove to this, holds the information of the Files for which we are showing results to
    private ArrayList<Result> currentResults = new ArrayList<>();//list of Results that it is currently showing
    private StartUpController startUpController;
    private ListViewController listViewController;
    //TODO: show dialog when cant save pdf due to file in use, clean up class (eg unused lists)
    @Override
    public void start(Stage primaryStage) throws Exception {
        //need to start engine
        BackEndSystem.getInstance();//thread waits for this to be done
        System.out.println("Called getInstance");
        FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("startup.fxml"));
        primaryStage.setScene(new Scene(fxmlLoader.load(), 1024, 800));
        primaryStage.setTitle("Automated Timeline Extractor - Oliver Philip Hohn");
        startUpController = fxmlLoader.getController();
        startUpController.setObserver(this);
        primaryStage.show();

        this.primaryStage = primaryStage;
    }

    public static void main(String[] args) {
        launch(args);
    }


    private List<File> loadFiles(Stage primaryStage) {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Open Document Files");
        fileChooser.getExtensionFilters().addAll(new FileChooser.ExtensionFilter("Text Files", "*.txt", "*.pdf", "*.docx"));
        return fileChooser.showOpenMultipleDialog(primaryStage);
    }

    private List<FileData> getFileData(List<File> files) {
        ArrayList<FileData> toReturn = new ArrayList<>();
        for (File file : files) {
            toReturn.add(new FileData(file));
        }
        return toReturn;
    }

    private Task<List<Result>> prepareTask(List<File> files, List<FileData> fileDatas) {
        return new Task<List<Result>>() {
            @Override
            protected List<Result> call() throws Exception {
                ProcessFiles processFiles = new ProcessFiles();
                return processFiles.processFiles(files, fileDatas);
            }
        };
    }

    private ListViewController showListView(Stage stage) {
        FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("listView.fxml"));
        try {
            startUpController = null;
            primaryStage.setScene(new Scene(fxmlLoader.load(), primaryStage.getWidth(), primaryStage.getHeight()));
            primaryStage.setTitle("Automated Timeline Extractor - Oliver Philip Hohn");
            listViewController = fxmlLoader.getController();
            listViewController.setTimelineObserver(this);
            primaryStage.show();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return listViewController;
    }


    @Override
    public void loadFiles() {
        List<File> files = loadFiles(primaryStage);
        if (files != null) {
            List<FileData> fileDatas = getFileData(files);
            Alert fileConfirmationDialog = new FileConfirmationDialog().getConfirmationFileDialog(fileDatas);
            Optional<ButtonType> response = fileConfirmationDialog.showAndWait();
            if (response.get() == ButtonType.OK) {
                //disable button
                startUpController.setDisableLoadDocumentsButton(true);
                System.out.println(TAG + "Process Files and set them in the Timeline");
                Task<List<Result>> task = prepareTask(files, fileDatas);
                task.setOnSucceeded(new EventHandler<WorkerStateEvent>() {
                    @Override
                    public void handle(WorkerStateEvent event) {
                        List<Result> results = task.getValue();
                        listViewController = showListView(primaryStage);
                        listViewController.setTimelineListView(results, fileDatas);
                        primaryStage.show();
                    }
                });
                new Thread(task).start();
            } else {
                System.out.println(TAG + "Dont Process Files and set them in the Timeline");
            }
        }
    }

    @Override
    public void showAbout() {
        System.out.println(TAG + "show about information");
    }

    @Override
    public void close() {
        System.out.println(TAG + "close program");
        primaryStage.close();
    }

    @Override
    public void timeline() {
        System.out.println(TAG + "timeline options");
    }


    @Override
    public void loadDocuments() {
        List<File> files = loadFiles(primaryStage);
        if (files != null) {
            List<FileData> fileDatas = getFileData(files);
            Alert fileConfirmationDialog = new FileConfirmationDialog().getConfirmationFileDialog(fileDatas);
            Optional<ButtonType> response = fileConfirmationDialog.showAndWait();
            if (response.get() == ButtonType.OK) {
                System.out.println(TAG + "Process Files and Add them to the Timeline");
                Task<List<Result>> task = prepareTask(files, fileDatas);
                task.setOnSucceeded(new EventHandler<WorkerStateEvent>() {
                    @Override
                    public void handle(WorkerStateEvent event) {
                        List<Result> results = task.getValue();
                        if (listViewController != null) {
                            listViewController.addToTimelineListView(results, fileDatas);
                        }
                    }
                });
                new Thread(task).start();
            } else {
                System.out.println(TAG + "Don't process Files");
            }
        }
    }

    @Override
    public void saveToPDF(List<Result> results) {
        System.out.println(TAG + "Save To PDF pressed");
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Save Timeline As...");
        fileChooser.getExtensionFilters().addAll(new FileChooser.ExtensionFilter("PDF File","*.pdf"));
        File file = fileChooser.showSaveDialog(primaryStage);
        if(file != null && listViewController != null){
            try {
                new ToPDF().saveToPDF(results, file);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}
