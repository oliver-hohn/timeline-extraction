package frontend.dialogs;

import backend.system.BackEndSystem;
import backend.system.Settings;
import javafx.event.EventHandler;
import javafx.geometry.Insets;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.GridPane;
import javafx.scene.text.Text;
import javafx.util.Callback;
import javafx.util.StringConverter;

import java.util.Optional;

/**
 * Class to create the Settings Dialog.
 */
public class SettingsDialog {
    private Spinner<Integer> threadCountSpinner;
    private Spinner<Integer> thresholdSpinner;
    private Spinner<Integer> widthSpinner;
    private Spinner<Integer> heightSpinner;

    /**
     * Get a Settings Dialog, that will allow the user to change the Settings of the System and either Save them or
     * discard them. The option to return to the Default Settings is also added. The changed Settings would be applied
     * to the cloned Settings held in the BackEndSystem, which should be then set to the Settings of the System.
     *
     * @return the Settings Dialog.
     * @throws CloneNotSupportedException when cloning the BackEndSystem Settings.
     */
    public Dialog<Settings> settingsDialog() throws CloneNotSupportedException {
        Settings copy = (Settings) BackEndSystem.getInstance().getSettings().clone();
        Dialog<Settings> settingsDialog = new Dialog<>();
        //set up dialog layout
        settingsDialog.setTitle("Preferences");
        settingsDialog.setHeaderText(null);
        settingsDialog.getDialogPane().setContent(getDialogLayout(copy));

        //buttons
        ButtonType buttonTypeSave = new ButtonType("Save", ButtonBar.ButtonData.OK_DONE);
        ButtonType buttonTypeCancel = new ButtonType("Cancel", ButtonBar.ButtonData.CANCEL_CLOSE);
        ButtonType buttonTypeDefault = new ButtonType("Default", ButtonBar.ButtonData.LEFT);
        settingsDialog.getDialogPane().getButtonTypes().setAll(buttonTypeDefault, buttonTypeSave, buttonTypeCancel);

        //set default onaction
        Node defaultButton = settingsDialog.getDialogPane().lookupButton(buttonTypeDefault);
        defaultButton.setOnMousePressed(new EventHandler<MouseEvent>() {
            @Override
            public void handle(MouseEvent event) {
                Alert confirmation = new Alert(Alert.AlertType.CONFIRMATION);
                confirmation.setTitle("Reset to Default?");
                confirmation.setHeaderText(null);
                confirmation.setContentText("Are you sure you wish reset the Settings to Default? ");
                confirmation.getDialogPane().getButtonTypes().setAll(ButtonType.YES, ButtonType.NO);
                Optional<ButtonType> response = confirmation.showAndWait();
                if (response.isPresent() && response.get() == ButtonType.YES) {
                    //reset to default
                    copy.reset();
                    settingsDialog.getDialogPane().setContent(getDialogLayout(copy));
                }
            }
        });

        //set result
        settingsDialog.setResultConverter(new Callback<ButtonType, Settings>() {
            @Override
            public Settings call(ButtonType param) {
                if (param == buttonTypeSave) {
                    copy.setWidth(widthSpinner.getValue());
                    copy.setHeight(heightSpinner.getValue());
                    copy.setThresholdSummary(thresholdSpinner.getValue());
                    copy.setMaxNoOfThreads(threadCountSpinner.getValue());
                    return copy;
                }
                return null;
            }
        });
        return settingsDialog;
    }

    /**
     * For the Settings Dialog, get its Content Root Layout: a GridPane with Text and Spinners for the different Settings
     * options. The given Settings are used to populate the fields in the layout.
     *
     * @param settings the given Settings.
     * @return the root Content layout of the Dialog.
     */
    private GridPane getDialogLayout(Settings settings) {
        GridPane gridPane = new GridPane();
        gridPane.setMaxWidth(Double.MAX_VALUE);
        gridPane.setPadding(new Insets(10));
        gridPane.setHgap(10);
        gridPane.setVgap(10);

        Text fileTitle = new Text("File Processing");
        gridPane.add(fileTitle, 0, 0);

        Text threadText = new Text("Maximum Number of Threads running in parallel: ");
        threadCountSpinner = new Spinner<>();
        SpinnerValueFactory.IntegerSpinnerValueFactory spinnerValueFactory = new SpinnerValueFactory.IntegerSpinnerValueFactory(1, 20, settings.getMaxNoOfThreads());
        spinnerValueFactory.setAmountToStepBy(1);
        spinnerValueFactory.setConverter(new IntegerStringConverter(threadCountSpinner));
        threadCountSpinner.setValueFactory(spinnerValueFactory);
        threadCountSpinner.setEditable(true);
        gridPane.add(threadText, 0, 1);
        gridPane.add(threadCountSpinner, 1, 1);

        Text thresholdText = new Text("Threshold of Text Summary: ");
        thresholdSpinner = new Spinner<>();
        spinnerValueFactory = new SpinnerValueFactory.IntegerSpinnerValueFactory(1, 20, settings.getThresholdSummary());
        spinnerValueFactory.setAmountToStepBy(1);
        spinnerValueFactory.setConverter(new IntegerStringConverter(thresholdSpinner));
        thresholdSpinner.setValueFactory(spinnerValueFactory);
        thresholdSpinner.setEditable(true);
        gridPane.add(thresholdText, 0, 2);
        gridPane.add(thresholdSpinner, 1, 2);

        Separator separator = new Separator();//by default its horizontal
        gridPane.add(separator, 0, 3, 2, 1);

        Text appearanceText = new Text("Appearance");
        gridPane.add(appearanceText, 0, 4);

        Text widthText = new Text("Width at Startup: ");
        widthSpinner = new Spinner<>();
        spinnerValueFactory = new SpinnerValueFactory.IntegerSpinnerValueFactory(720, 1920, settings.getWidth());
        spinnerValueFactory.setAmountToStepBy(5);
        spinnerValueFactory.setConverter(new IntegerStringConverter(widthSpinner));
        widthSpinner.setValueFactory(spinnerValueFactory);
        widthSpinner.setEditable(true);
        gridPane.add(widthText, 0, 5);
        gridPane.add(widthSpinner, 1, 5);

        Text heightText = new Text("Height at Startup: ");
        heightSpinner = new Spinner<>();
        spinnerValueFactory = new SpinnerValueFactory.IntegerSpinnerValueFactory(600, 1080, settings.getHeight());
        spinnerValueFactory.setAmountToStepBy(5);
        spinnerValueFactory.setConverter(new IntegerStringConverter(heightSpinner));
        heightSpinner.setValueFactory(spinnerValueFactory);
        heightSpinner.setEditable(true);
        gridPane.add(heightText, 0, 6);
        gridPane.add(heightSpinner, 1, 6);

        return gridPane;
    }

    /**
     * Private Class to deal with the conversion of Strings to Ints in the Spinner text fields.
     */
    private static class IntegerStringConverter extends StringConverter<Integer> {
        Spinner<Integer> spinner;

        /**
         * Constructor that takes in the Spinner where the changes will be applied on.
         *
         * @param spinner the given Spinner.
         */
        IntegerStringConverter(Spinner<Integer> spinner) {
            this.spinner = spinner;
        }

        /**
         * For the given Integer held in the Spinner, show its String version.
         *
         * @param object the given Integer.
         * @return the Integer as a String.
         */
        @Override
        public String toString(Integer object) {
            return String.valueOf(object);
        }

        /**
         * For the given String (in the textfield) attempt to turn it into an Integer, if that is not possible, use the
         * previous value held by the Spinner (which would be an Integer).
         *
         * @param string the text in the textfield.
         * @return an Integer made from the text in the textfield, or the previous available Integer.
         */
        @Override
        public Integer fromString(String string) {
            Integer integer;
            try {
                integer = Integer.parseInt(string);
            } catch (Exception e) {
                integer = spinner.getValue();
            }
            return integer;
        }
    }
}
