package frontend.controllers;

import frontend.observers.StartUpObserver;
import javafx.fxml.Initializable;

import java.net.URL;
import java.util.ResourceBundle;

/**
 * Controller for the Startup Scene
 */
//hold observer of main, to update it when the buttons are pressed (to load documents)
public class StartUpController implements Initializable, MenuBarControllerInter {
    private final static String TAG = "STARTUPCONTROLLER: ";
    private StartUpObserver observer;
    @Override
    public void initialize(URL location, ResourceBundle resources) {
        System.out.println(TAG+"StartUp fxml is starting to run");

    }

    public void setObserver(StartUpObserver observer){
        System.out.println("Observer has been set");
        this.observer = observer;
    }

    public void loadDocuments(){
        System.out.println(TAG+"load documents");
        if(observer != null){
            observer.loadFiles();//should disable button to not load in more files while doing this
        }
    }

    @Override
    public void close() {
        if(observer != null){
            observer.close();
        }
    }

    @Override
    public void about() {
        if(observer != null){
            observer.showAbout();
        }
    }

    @Override
    public void timeline() {
        if(observer != null){
            observer.timeline();
        }
    }
}
