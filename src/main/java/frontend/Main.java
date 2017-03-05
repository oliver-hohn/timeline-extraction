package frontend;

import backend.ToJSON;
import backend.ToPDF;
import backend.process.FileData;
import backend.process.ProcessFiles;
import backend.process.Result;
import backend.system.BackEndSystem;
import backend.system.Settings;
import frontend.controllers.ListViewController;
import frontend.controllers.StartUpController;
import frontend.dialogs.AboutDialog;
import frontend.dialogs.FileConfirmationDialog;
import frontend.dialogs.SettingsDialog;
import frontend.observers.StartUpObserver;
import frontend.observers.TimelineObserver;
import javafx.application.Application;
import javafx.concurrent.Task;
import javafx.concurrent.WorkerStateEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonBar;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Dialog;
import javafx.stage.FileChooser;
import javafx.stage.Stage;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.function.Consumer;

/**
 * Main class that is used to run the program (i.e. show the UI that uses the backend). It handles loading the different
 * layouts, and being the observer of the layouts controllers.
 */
public class Main extends Application implements StartUpObserver, TimelineObserver {
    private final static String TAG = "MAIN: ";
    private Stage primaryStage;
    private StartUpController startUpController;
    private ListViewController listViewController;

    /**
     * Called to start showing the window of the program (i.e. the please load documents layout).
     *
     * @param primaryStage the root window of the application
     * @throws Exception
     */
    @Override
    public void start(Stage primaryStage) throws Exception {
        //need to start engine
        BackEndSystem.getInstance();//thread waits for this to be done
        Settings settings = BackEndSystem.getInstance().getSettings();
        System.out.println("Called getInstance");
        FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("startup.fxml"));
        primaryStage.setScene(new Scene(fxmlLoader.load(), settings.getWidth(), settings.getHeight()));
        primaryStage.setTitle("Automated Timeline Extractor - Oliver Philip Hohn");
        startUpController = fxmlLoader.getController();
        startUpController.setObserver(this);
        primaryStage.show();
        this.primaryStage = primaryStage;
    }

    /**
     * First method that gets called when the program runs.
     *
     * @param args the arguments passed in when the instruction to run the program is given.
     */
    public static void main(String[] args) {
        launch(args);
    }

    /**
     * Produces a list of Files, picked by the user in the File Chooser.
     *
     * @param primaryStage the window where the program is running on.
     * @return a list of Files picked by the user in the File Chooser.
     */
    private List<File> loadFiles(Stage primaryStage) {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Choose Document Files");
        fileChooser.getExtensionFilters().addAll(new FileChooser.ExtensionFilter("Text Files", "*.txt", "*.pdf", "*.docx"));
        return fileChooser.showOpenMultipleDialog(primaryStage);
    }

    /**
     * For the given List of Files produce a List of FileData, where each FileData corresponds to its File (i.e. have the
     * same index in the list), and holds its Files name, and path.
     *
     * @param files the given List of Files.
     * @return the output List of FileData.
     */
    private List<FileData> getFileData(List<File> files) {
        ArrayList<FileData> toReturn = new ArrayList<>();
        for (File file : files) {
            toReturn.add(new FileData(file));
        }
        return toReturn;
    }

    /**
     * Called to produce a Task object, that will run a set of operations, when given to a Thread, in parallel. This
     * Task object will, for the given Lists of Files and FileData, produce the List of Results that emerge from
     * processing the text in the Files, and linking each Result object to its corresponding FileData.
     *
     * @param files     the given List of Files.
     * @param fileDatas the given List of FileData.
     * @return a Task object to run in a Thread in parallel, to process the given Files and produce Result objects.
     */
    private Task<List<Result>> prepareTask(List<File> files, List<FileData> fileDatas) {
        return new Task<List<Result>>() {
            @Override
            protected List<Result> call() throws Exception {
                ProcessFiles processFiles = new ProcessFiles();
                return processFiles.processFiles(files, fileDatas);
            }
        };
    }

    /**
     * For the global Stage, load the listView layout, set its Observer as Main.this, and hold its controller.
     *
     * @return the Controller of the listView layout.
     */
    private ListViewController showListView() {
        FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("listView.fxml"));
        try {
            primaryStage.setScene(new Scene(fxmlLoader.load(), primaryStage.getWidth(), primaryStage.getHeight()));
            listViewController = fxmlLoader.getController();
            listViewController.setTimelineObserver(this);
            primaryStage.show();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return listViewController;
    }


    /**
     * Called from the Controller of the initial layout (the "load-button-layout"), to indicate to Main, that it must
     * provide the user the option to allow the User to pick Files, pass those Files to the backend to process, and
     * then update the layout to show the timeline of the events depicted in the text of the Files.
     * The button to load Files is disabled, to stop the user from pressing it again, while the Files are being loaded.
     */
    @Override
    public void loadFiles() {
        List<File> files = loadFiles(primaryStage);
        if (files != null) {
            List<FileData> fileDatas = getFileData(files);
            Alert fileConfirmationDialog = new FileConfirmationDialog().getConfirmationFileDialog(fileDatas);
            Optional<ButtonType> response = fileConfirmationDialog.showAndWait();
            if (response.isPresent() && response.get() == ButtonType.OK) {
                //disable button
                startUpController.setDisableLoadDocumentsButton(true);
                System.out.println(TAG + "Process Files and set them in the Timeline");
                Task<List<Result>> task = prepareTask(files, fileDatas);
                task.setOnSucceeded(new EventHandler<WorkerStateEvent>() {
                    @Override
                    public void handle(WorkerStateEvent event) {
                        List<Result> results = task.getValue();
                        listViewController = showListView();
                        listViewController.setTimelineListView(results, fileDatas);
                        //stop showing the loading dialog as we have the other layout ready to show
                        startUpController.removeLoadingDialog();
                        startUpController = null;
                        primaryStage.show();
                    }
                });
                task.setOnRunning(new EventHandler<WorkerStateEvent>() {
                    @Override
                    public void handle(WorkerStateEvent event) {
                        //show loading dialog
                        startUpController.showLoadingDialog();
                    }
                });
                new Thread(task).start();
            } else {
                System.out.println(TAG + "Dont Process Files and set them in the Timeline");
            }
        }
    }

    /**
     * Called when the About MenuItem is pressed. Called from either the ListViewController or the StartUpController.
     */
    @Override
    public void showAbout() {
        System.out.println(TAG + "show about information");
        new AboutDialog().getAboutDialog().showAndWait();
    }

    /**
     * Called when the Close MenuItem is pressed. Should close the programs (root) window.
     * Called from either the ListViewController or the StartUpController.
     */
    @Override
    public void close() {
        System.out.println(TAG + "close program");
        primaryStage.close();
    }

    /**
     * Called when the Timeline MenuItem is pressed. Called from either the ListViewController or the StartUpController.
     * But it is only available from the ListView layout, as in the StartUp layout it is disabled.
     */
    @Override
    public void timeline() {
        System.out.println(TAG + "timeline options");
    }

    /**
     * Called when the Preferences MenuItem is pressed in the File Menu.
     */
    @Override
    public void preferences() {
        System.out.println(TAG + "Preferences pressed");
        try {
            Dialog<Settings> settingsDialog = new SettingsDialog().settingsDialog();//show a settings dialog
            Optional<Settings> response = settingsDialog.showAndWait();
            response.ifPresent(new Consumer<Settings>() {
                @Override
                public void accept(Settings settings) {
                    if (settings != null) {//then the user decided to save the settings
                        BackEndSystem.getInstance().setSettings(settings);
                    }//else dont apply the Settings to the System.
                }
            });
        } catch (CloneNotSupportedException e) {
            e.printStackTrace();
        }
    }

    /**
     * Called by the StartUpController, to indicate to Main, to allow the User to pick Files (from the File Chooser),
     * process them, and add them to the timeline (instead of setting them like in the previous method). The layout is
     * not changed, as we are already in the timeline layout.
     */
    @Override
    public void loadDocuments() {
        List<File> files = loadFiles(primaryStage);
        if (files != null) {
            List<FileData> fileDatas = getFileData(files);
            Alert fileConfirmationDialog = new FileConfirmationDialog().getConfirmationFileDialog(fileDatas);
            Optional<ButtonType> response = fileConfirmationDialog.showAndWait();
            if (response.isPresent() && response.get() == ButtonType.OK) {
                System.out.println(TAG + "Process Files and Add them to the Timeline");
                Task<List<Result>> task = prepareTask(files, fileDatas);
                task.setOnSucceeded(new EventHandler<WorkerStateEvent>() {
                    @Override
                    public void handle(WorkerStateEvent event) {
                        List<Result> results = task.getValue();
                        if (listViewController != null) {
                            listViewController.addToTimelineListView(results, fileDatas);
                            //stop showing loading dialog as we have added the results
                            listViewController.removeLoadingDialog();
                        }
                    }
                });
                task.setOnRunning(new EventHandler<WorkerStateEvent>() {
                    @Override
                    public void handle(WorkerStateEvent event) {
                        //show loading dialog
                        listViewController.showLoadingDialog();
                    }
                });
                new Thread(task).start();
            } else {
                System.out.println(TAG + "Don't process Files");
            }
        }
    }


    /**
     * Used to show the Alert the dialog to allow the User to pick in what format to save the List of Results (JSON or
     * PDF). Depending on the option selected, the FileChooser is shown, and then the User picks the location to save
     * the file. An Alert is shown if the User is trying to overwrite a File in use when saving.
     *
     * @param results the List of Results to Save.
     */
    @Override
    public void saveTo(List<Result> results) {
        //show alert to know what to save as
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Save to...");
        alert.setHeaderText(null);
        alert.setContentText("Do you wish to save as PDF or JSON?");
        ButtonType buttonTypePDF = new ButtonType("Save to PDF");
        ButtonType buttonTypeJSON = new ButtonType("Save to JSON");
        ButtonType buttonTypeCancel = new ButtonType("Cancel", ButtonBar.ButtonData.CANCEL_CLOSE);
        alert.getDialogPane().getButtonTypes().setAll(buttonTypePDF, buttonTypeJSON, buttonTypeCancel);

        Optional<ButtonType> response = alert.showAndWait();
        if (response.isPresent() && response.get() == buttonTypePDF) {
            saveToPDF(results);
        } else if (response.isPresent() && response.get() == buttonTypeJSON) {
            saveToJSON(results);
        }
    }

    /**
     * Called when the Timeline being displayed (described by the List of Result objects) needs to be saved as a JSON.
     * The System should allow the user to pick a location to save the PDF, if it is overwriting a File in use by
     * another process the user is informed and given the option to select another location or close the process using
     * the File that they wish to overwrite.
     *
     * @param results the given List of Result objects.
     */
    private void saveToJSON(List<Result> results) {
        String json = ToJSON.toJSON(results);
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Save Timeline As...");
        fileChooser.getExtensionFilters().addAll(new FileChooser.ExtensionFilter("JSON File", "*.json"));
        File file = fileChooser.showSaveDialog(primaryStage);
        if (file != null && listViewController != null) {
            try {
                PrintWriter printWriter = new PrintWriter(file);
                printWriter.write(json);
                printWriter.close();
            } catch (Exception e) {
                Alert alert = new Alert(Alert.AlertType.INFORMATION);//then inform the user,
                alert.setTitle("File In Use");
                alert.setHeaderText(null);
                alert.setContentText("The file: " + file.getName() + " is in use by another process.");
                alert.getDialogPane().getButtonTypes().setAll(ButtonType.OK);
                Optional<ButtonType> response = alert.showAndWait();
                if (response.isPresent() && response.get() == ButtonType.OK) {//and if they press OK, ie want to save
                    saveToJSON(results);//show them the file chooser to let them pick a different (or same location, if
                }
            }
        }
    }

    /**
     * Called when the Timeline being displayed (described by the List of Result objects) needs to be saved as a PDF.
     * The System should allow the user to pick a location to save the PDF, if it is overwriting a File in use by
     * another process the user is informed and given the option to select another location or close the process using
     * the File that they wish to overwrite.
     *
     * @param results the given List of Result objects.
     */
    private void saveToPDF(List<Result> results) {
        System.out.println(TAG + "Save To PDF pressed");
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Save Timeline As...");
        fileChooser.getExtensionFilters().addAll(new FileChooser.ExtensionFilter("PDF File", "*.pdf"));
        File file = fileChooser.showSaveDialog(primaryStage);
        if (file != null && listViewController != null) {
            try {
                new ToPDF().saveToPDF(results, file);
            } catch (IOException e) {//if cant save the file, because it is most probably in use or it has been deleted
                Alert alert = new Alert(Alert.AlertType.INFORMATION);//then inform the user,
                alert.setTitle("File In Use");
                alert.setHeaderText(null);
                alert.setContentText("The file: " + file.getName() + " is in use by another process.");
                alert.getDialogPane().getButtonTypes().setAll(ButtonType.OK);
                Optional<ButtonType> response = alert.showAndWait();
                if (response.isPresent() && response.get() == ButtonType.OK) {//and if they press OK, ie want to save
                    saveToPDF(results);//show them the file chooser to let them pick a different (or same location, if
                }                               //they closed the process
            }
        }
    }
}
