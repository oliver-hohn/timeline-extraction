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

    public LoadingDialog(StackPane stackPane, Pane mainLayout){
        this.stackPane = stackPane;
        this.mainLayout = mainLayout;//the layout on which we are adding on top the loading dialog
    }

    private void initializeLoadingDialog(){
        if(this.loadingDialog == null){
            VBox loadingDialog = new VBox();//main layout to hold the dialog
            loadingDialog.getStyleClass().add("loading-dialog");//for the css
            loadingDialog.setPadding(new Insets(20,15,20,15));//add padding
            loadingDialog.getStylesheets().add(getClass().getResource("loadingDialog.css").toExternalForm());//load the css styles
            //add the "title" of the dialog
            Text pleaseWaitText = new Text("Please Wait... ");
            pleaseWaitText.getStyleClass().add("please-wait-text");
            loadingDialog.getChildren().add(pleaseWaitText);
            loadingDialog.setMargin(pleaseWaitText, new Insets(0,0,10,0));
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

    public void showLoadingDialog(){
        initializeLoadingDialog();
        if(stackPane != null && mainLayout != null){
            mainLayout.setDisable(true);
            stackPane.getChildren().add(loadingDialog);
        }
    }

    public void removeLoadingDialog(){
        if(stackPane != null && mainLayout != null){
            mainLayout.setDisable(false);
            if(loadingDialog != null){
                stackPane.getChildren().remove(loadingDialog);
            }
        }
    }
}
