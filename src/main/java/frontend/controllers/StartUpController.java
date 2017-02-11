package frontend.controllers;

import frontend.dialogs.LoadingDialog;
import frontend.observers.StartUpObserver;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;

import java.net.URL;
import java.util.ResourceBundle;

/**
 * Controller for the Startup Scene. Holds a StartUpObserver, where it calls the relevant methods, when the Controller is
 * called. As the scene has a menu bar, it implements the MenuBarControllerInterface, by implementing the relevant methods
 * for the menu bar.
 */
public class StartUpController implements Initializable, MenuBarControllerInter {
    private final static String TAG = "STARTUPCONTROLLER: ";
    private StartUpObserver observer;
    @FXML
    private Button loadDocumentsButton;
    @FXML
    private StackPane stackPane;
    @FXML
    private VBox vBox;//main layout
    private LoadingDialog loadingDialog;

    /**
     * Called on creation of the Scene.
     */
    @Override
    public void initialize(URL location, ResourceBundle resources) {
        System.out.println(TAG + "StartUp fxml is starting to run");
        loadingDialog = new LoadingDialog(stackPane, vBox);
    }

    /**
     * Set the StartUpObserver to be used when the Controller has to process an action.
     *
     * @param observer the StartUpObserver linked to this Controller.
     */
    public void setObserver(StartUpObserver observer) {
        System.out.println("Observer has been set");
        this.observer = observer;
    }

    /**
     * Called when the button to load documents is pressed.
     */
    public void loadDocuments() {
        System.out.println(TAG + "load documents");
        if (observer != null) {
            observer.loadFiles();//should disable button to not load in more files while doing this
        }
    }

    /**
     * When the close menu item is pressed.
     */
    @Override
    public void close() {
        if (observer != null) {
            observer.close();
        }
    }

    /**
     * When the about menu item is pressed.
     */
    @Override
    public void about() {
        if (observer != null) {
            observer.showAbout();
        }
    }

    /**
     * When the timeline menu item is pressed.
     */
    @Override
    public void timeline() {
        if (observer != null) {
            observer.timeline();
        }
    }

    /**
     * When the preferences menu item is pressed.
     */
    @Override
    public void preferences() {
        if (observer != null) {
            observer.preferences();
        }
    }

    /**
     * Set whether or not the Load Documents Button should be disabled (so that it cannot be pressed)
     *
     * @param disable whether or not the Load Documents Button should be disabled.
     */
    public void setDisableLoadDocumentsButton(boolean disable) {
        loadDocumentsButton.setDisable(disable);
    }

    public void showLoadingDialog(){
        loadingDialog.showLoadingDialog();
    }

    public void removeLoadingDialog(){
        loadingDialog.removeLoadingDialog();
    }
}
