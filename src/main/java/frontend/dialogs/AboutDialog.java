package frontend.dialogs;

import javafx.scene.control.ButtonType;
import javafx.scene.control.Dialog;
import javafx.scene.control.Label;
import javafx.scene.layout.GridPane;

/**
 * Class to make the About Dialog that shows information of the Application.
 */
public class AboutDialog {
    private final static String AUTHOR = "Oliver Philip Hohn";
    private final static String APP_NAME = "Automated Timeline Extraction";
    private final static String DEFAULT_VERSION = "DEVELOPMENT";

    /**
     * Get a Dialog that shows the information of the System (the version number, who created it, etc.)
     *
     * @return a Dialog that shows the information of the System.
     */
    public Dialog getAboutDialog() {
        Dialog aboutDialog = new Dialog();
        aboutDialog.setTitle("About");
        aboutDialog.setHeaderText(null);

        //layout of body gridpanes
        GridPane gridPane = new GridPane();
        String version = (getClass().getPackage().getImplementationVersion() != null) ? getClass().getPackage().getImplementationVersion() : DEFAULT_VERSION;
        Label appNameLabel = new Label("App Name: ");
        Label appLabel = new Label(APP_NAME + " v" + version);
        Label authorLabel = new Label(AUTHOR);
        Label authorNameLabel = new Label("Created By ");
        gridPane.add(appNameLabel, 0, 0);
        gridPane.add(appLabel, 1, 0);
        gridPane.add(authorNameLabel, 0, 1);
        gridPane.add(authorLabel, 1, 1);
        aboutDialog.getDialogPane().setContent(gridPane);
        aboutDialog.getDialogPane().getButtonTypes().setAll(ButtonType.OK);
        return aboutDialog;
    }

}
