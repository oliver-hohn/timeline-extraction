package frontend.controllers;

import frontend.observers.StartUpObserver;
import javafx.fxml.Initializable;

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

    /**
     * Called on creation of the Scene.
     */
    @Override
    public void initialize(URL location, ResourceBundle resources) {
        System.out.println(TAG + "StartUp fxml is starting to run");

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
}