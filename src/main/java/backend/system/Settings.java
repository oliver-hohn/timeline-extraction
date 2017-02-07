package backend.system;


import com.google.gson.*;
import com.google.gson.annotations.Expose;

import java.io.*;
import java.net.URISyntaxException;
import java.util.Scanner;

/**
 * Class that represents the possible Settings of the System.
 * Settings are attempted to be retrieved and/or stored in a Settings file, where the data is encoded in JSON.
 */
public class Settings implements Cloneable {
    //default values (to reset to default, or if settings file not found)
    public final static int defaultMaxNoThreads = 2;
    public final static int defaultThresholdSummary = 10;
    public final static int defaultWidth = 1024;
    public final static int defaultHeight = 800;
    private final static String threadTag = "maxNoOfThreads";
    private final static String thresholdTag = "thresholdSummary";
    private final static String widthTag = "width";
    private final static String heightTag = "height";
    //actual values
    @Expose(deserialize = false)
    private int maxNoOfThreads;
    @Expose(deserialize = false)
    private int thresholdSummary;
    @Expose(deserialize = false)
    private int width;
    @Expose(deserialize = false)
    private int height;

    /**
     * Used to create an instance of the Settings of this System, by attempting to load the Settings file.
     * If this can't be done the default values of the Settings are used to populate the fields.
     *
     * @param loadFromFile whether or not the Settings File should be loaded.
     */
    public Settings(boolean loadFromFile) {
        //try to read from settings file, if not possible, then use default values
        if (!loadFromFile || !loadSettingsFile()) {
            reset();
        }
    }

    /**
     * Used to create an instance of the Settings of this System, where the fields are populated by the default values.
     */
    public Settings() {
        this(false);
    }

    /**
     * Attempt to load the Settings from the Settings file.
     *
     * @return whether or not the Settings file could be retrieved and the fields of the Settings be populated.
     */
    private boolean loadSettingsFile() {
        System.out.println("Trying to load from file");
        //load the file, return true if it could
        try {
            File settingsFile = new File("settings.ini");
            //now need to read settings file
            System.out.println("Could find settings.ini file");
            String text = getJSONString(settingsFile);
            System.out.println("String in file: " + text);
            JsonElement jsonElement = new JsonParser().parse(text);
            JsonObject jsonObject = jsonElement.getAsJsonObject();
            System.out.println(jsonObject);
            setValues(jsonObject);
        } catch (Exception e) {
            return false;
        }
        return true;
    }

    /**
     * For the given File attempt to retrieve the JSON stored in it.
     *
     * @param file the given File (which should have the JSON settings data).
     * @return a String of the JSON encoding of the Settings in the File.
     * @throws FileNotFoundException thrown if the File could not be found.
     */
    private String getJSONString(File file) throws FileNotFoundException {
        Scanner scanner = new Scanner(file);
        StringBuilder stringBuilder = new StringBuilder();
        while (scanner.hasNext()) {
            stringBuilder.append(scanner.nextLine());
        }
        scanner.close();
        return stringBuilder.toString();
    }

    /**
     * For the given JsonObject, populate the Settings field with its data, if its present, else use the default values.
     *
     * @param jsonObject the given JsonObject that should have the Settings data.
     */
    private void setValues(JsonObject jsonObject) {
        maxNoOfThreads = (jsonObject.get(threadTag) != null) ? jsonObject.get(threadTag).getAsInt() : defaultMaxNoThreads;
        thresholdSummary = (jsonObject.get(thresholdTag) != null) ? jsonObject.get(thresholdTag).getAsInt() : defaultThresholdSummary;
        width = (jsonObject.get(widthTag) != null) ? jsonObject.get(widthTag).getAsInt() : defaultWidth;
        height = (jsonObject.get(heightTag) != null) ? jsonObject.get(heightTag).getAsInt() : defaultHeight;
        if (!isConstrained()) {//if constraints have not been set then reset
            reset();
        }
    }


    /**
     * For the Settings fields in this case, store them in JSON format in the Settings File.
     *
     * @return whether or not the Settings of this File could be saved in a File.
     */
    public boolean saveSettingsFile() {
        final GsonBuilder gsonBuilder = new GsonBuilder();
        gsonBuilder.excludeFieldsWithoutExposeAnnotation();
        final Gson gson = gsonBuilder.create();
        try {
            String json = gson.toJson(this);
            System.out.println(json);
            saveToSettingsFile(json);
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
        return true;
    }

    /**
     * Save the given text in the "settings.ini" File.
     *
     * @param text the given Text
     * @throws IOException if an error occurs while accessing the File
     */
    private void saveToSettingsFile(String text) throws IOException {
        PrintWriter writer = new PrintWriter("settings.ini");
        writer.println(text);
        writer.close();
    }

    /**
     * For all the Fields in the Settings class, set their values to the default values.
     */
    public void reset() {
        //resets all the values to default
        maxNoOfThreads = defaultMaxNoThreads;
        thresholdSummary = defaultThresholdSummary;
        width = defaultWidth;
        height = defaultHeight;
    }

    /**
     * Getter for the Max Number of Threads that are allowed to run in parallel in this System.
     *
     * @return the Max Number of Threads that are allowed to run in parallel in this System.
     */
    public int getMaxNoOfThreads() {
        return maxNoOfThreads;
    }

    /**
     * Setter for the Max Number of Threads that are allowed to run in parallel in this System.
     *
     * @param maxNoOfThreads the Max Number of Threads that are allowed to run in parallel in this System.
     */
    public void setMaxNoOfThreads(int maxNoOfThreads) {
        if (isMaxNoOfThreadsConstrained(maxNoOfThreads)) {
            this.maxNoOfThreads = maxNoOfThreads;
        }
    }

    /**
     * Getter for the Threshold used when attempting to produce a Summary of a Sentence.
     *
     * @return the Threshold used when attempting to produce a Summary of a Sentence.
     */
    public int getThresholdSummary() {
        return thresholdSummary;
    }

    /**
     * Setter for the Threshold used when attempting to produce a Summary of a Sentence.
     *
     * @param thresholdSummary the Threshold used when attempting to produce a Summary of a Sentence.
     */
    public void setThresholdSummary(int thresholdSummary) {
        if (isThresholdConstrained(thresholdSummary)) {
            this.thresholdSummary = thresholdSummary;
        }
    }

    /**
     * Getter for the preferred start up width of the Program window.
     *
     * @return the preferred start up width of the Program window.
     */
    public int getWidth() {
        return width;
    }

    /**
     * Setter for the preferred start up width of the Program window.
     *
     * @param width the preferred start up width of the Program window.
     */
    public void setWidth(int width) {
        if (isWidthConstrained(width)) {
            this.width = width;
        }
    }

    /**
     * Getter for the preferred start up height of the Program window.
     *
     * @return the preferred start up height of the Program window.
     */
    public int getHeight() {
        return height;
    }

    /**
     * Setter for the preferred start up height of the Program window.
     *
     * @param height the preferred start up height of the Program window.
     */
    public void setHeight(int height) {
        if (isHeightConstrained(height)) {
            this.height = height;
        }
    }

    /**
     * Produce a clone of this Object, such that the populated data in the clone is a clone of the data in this Object.
     *
     * @return a Clone of this Object (data values are the same but stored in a different area in memory)
     * @throws CloneNotSupportedException
     */
    @Override
    public Object clone() throws CloneNotSupportedException {
        super.clone();
        Settings clonedSettings = new Settings();
        clonedSettings.setHeight(height);
        clonedSettings.setMaxNoOfThreads(maxNoOfThreads);
        clonedSettings.setThresholdSummary(thresholdSummary);
        clonedSettings.setWidth(width);
        return clonedSettings;
    }

    /**
     * Whether or not the given threshold summary is within the constraints.
     *
     * @param thresholdSummary the given threshold summary
     * @return true if the threshold summary is within the constraints; false otherwise.
     */
    private boolean isThresholdConstrained(int thresholdSummary) {
        return thresholdSummary > 0 && thresholdSummary <= 20;
    }

    /**
     * Whether or not the given max number of threads is within the constraints.
     *
     * @param maxNoOfThreads the given max number of threads.
     * @return true if the max number of threads is within the constraints; false otherwise.
     */
    private boolean isMaxNoOfThreadsConstrained(int maxNoOfThreads) {
        return maxNoOfThreads > 0 && maxNoOfThreads <= 20;
    }

    /**
     * Whether or not the given width is within the constraints.
     *
     * @param width the given width.
     * @return true if the width is within the constraints; false otherwise.
     */
    private boolean isWidthConstrained(int width) {
        return width > 0 && width <= 1920;
    }

    /**
     * Whether or not the given height is within the constraints.
     *
     * @param height the given height.
     * @return true if the height is within the constraints; false otherwise.
     */
    private boolean isHeightConstrained(int height) {
        return height > 0 && height <= 1080;
    }

    /**
     * Whether or not all the fields in this Settings object are all within their constraints.
     *
     * @return true if all the fields are within their constraints; false otherwise.
     */
    private boolean isConstrained() {
        return isThresholdConstrained(thresholdSummary) && isMaxNoOfThreadsConstrained(maxNoOfThreads)
                && isWidthConstrained(width) && isHeightConstrained(height);
    }

}
