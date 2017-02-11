package frontend.dialogs;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.ProgressIndicator;
import javafx.scene.layout.*;
import javafx.scene.text.Text;

/**
 * Class used to show/hide the loading dialog in the current shown screen
 */
public class LoadingDialog {
    private StackPane stackPane;
    private VBox loadingDialog;
    private Pane mainLayout;

    /**
     * Creates a constructor used to show a loading dialog, and remove it.
     *
     * @param stackPane  the Root layout of the window. (Has to be stack pane, as we are adding a loading dialog on top
     *                   of the main layout of the window).
     * @param mainLayout the main content layout, which is disabled while the loading dialog is being shown (to avoid
     *                   the user from pressing buttons while the system is "Loading".
     */
    public LoadingDialog(StackPane stackPane, Pane mainLayout) {
        this.stackPane = stackPane;
        this.mainLayout = mainLayout;//the layout on which we are adding on top the loading dialog
    }

    /**
     * If the loading dialog (the VBox layout) has not been initialized, ie its null, then set it up: creates a layout
     * with text and a progress wheel, indicating the System is loading.
     */
    private void initializeLoadingDialog() {
        if (this.loadingDialog == null) {
            VBox loadingDialog = new VBox();//main layout to hold the dialog
            loadingDialog.getStyleClass().add("loading-dialog");//for the css
            loadingDialog.setPadding(new Insets(20, 15, 20, 15));//add padding
            loadingDialog.getStylesheets().add(getClass().getResource("loadingDialog.css").toExternalForm());//load the css styles
            //add the "title" of the dialog
            Text pleaseWaitText = new Text("Please Wait... ");
            pleaseWaitText.getStyleClass().add("please-wait-text");
            loadingDialog.getChildren().add(pleaseWaitText);
            loadingDialog.setMargin(pleaseWaitText, new Insets(0, 0, 10, 0));
            //the "body" of the dialog: the progress indicator and text
            GridPane gridPane = new GridPane();
            gridPane.setAlignment(Pos.CENTER);
            gridPane.setVgap(10);
            gridPane.setHgap(28);
            //add progress circle
            ProgressIndicator progressIndicator = new ProgressIndicator();
            progressIndicator.setProgress(-1.0f);
            gridPane.add(progressIndicator, 0, 0, 1, 1);
            //add the text
            Text processingText = new Text("Processing Files... ");
            processingText.getStyleClass().add("processing-text");
            gridPane.add(processingText, 1, 0, 2, 1);//want the text to be wider than the progress indicator
            //add the body
            loadingDialog.getChildren().add(gridPane);
            StackPane.setAlignment(loadingDialog, Pos.CENTER);//to the center of the given stack pane
            loadingDialog.setPrefSize(VBox.USE_COMPUTED_SIZE, VBox.USE_COMPUTED_SIZE);//so that the dialog is never squashed
            loadingDialog.setMaxWidth(VBox.USE_PREF_SIZE);//and never stretched, but instead fit to the size of its content
            loadingDialog.setMaxHeight(VBox.USE_PREF_SIZE);
            this.loadingDialog = loadingDialog;
        }
    }

    /**
     * Will initialize the loading layout if it hasn't been. If the provided StackPane and main layout Pane aren't null,
     * the loading dialog will be shown on top of the layout in the Stack Pane, and the main layout will be disabled
     * until the System finishes "loading".
     */
    public void showLoadingDialog() {
        initializeLoadingDialog();
        if (stackPane != null && mainLayout != null) {
            mainLayout.setDisable(true);
            stackPane.getChildren().add(loadingDialog);
        }
    }

    /**
     * Used to indicate the System has finished "loading". If the Pane's aren't null then the main layout Pane will be
     * enabled again, and the loading dialog will be removed from on top of the Stack Pane.
     * If the loadingDialog has not been shown, then nothing happens (the main layout is enabled - but it should already
     * be enabled).
     */
    public void removeLoadingDialog() {
        if (stackPane != null && mainLayout != null) {
            mainLayout.setDisable(false);
            if (loadingDialog != null) {
                stackPane.getChildren().remove(loadingDialog);
            }
        }
    }
}
