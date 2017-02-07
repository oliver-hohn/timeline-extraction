package backend.system;


import com.google.gson.*;
import com.google.gson.annotations.Expose;

import java.io.*;
import java.net.URISyntaxException;
import java.util.Scanner;

/**
 * Class that represents the possible Settings of the System.
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

    public Settings(boolean loadFromFile) {
        //try to read from settings file, if not possible, then use default values
        if (!loadFromFile || !loadSettingsFile()) {
            reset();
        }
    }

    public Settings() {
        this(false);
    }

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

    private String getJSONString(File file) throws FileNotFoundException {
        Scanner scanner = new Scanner(file);
        StringBuilder stringBuilder = new StringBuilder();
        while (scanner.hasNext()) {
            stringBuilder.append(scanner.nextLine());
        }
        scanner.close();
        return stringBuilder.toString();
    }

    private void setValues(JsonObject jsonObject) {
        maxNoOfThreads = (jsonObject.get(threadTag) != null) ? jsonObject.get(threadTag).getAsInt() : defaultMaxNoThreads;
        thresholdSummary = (jsonObject.get(thresholdTag) != null) ? jsonObject.get(thresholdTag).getAsInt() : defaultThresholdSummary;
        width = (jsonObject.get(widthTag) != null) ? jsonObject.get(widthTag).getAsInt() : defaultWidth;
        height = (jsonObject.get(heightTag) != null) ? jsonObject.get(heightTag).getAsInt() : defaultHeight;
        if(!isConstrained()){//if constraints have not been set then reset
            reset();
        }
    }


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

    private void saveToSettingsFile(String text) throws URISyntaxException, IOException {
        PrintWriter writer = new PrintWriter("settings.ini");
        writer.println(text);
        writer.close();
    }

    public void reset() {
        //resets all the values to default
        maxNoOfThreads = defaultMaxNoThreads;
        thresholdSummary = defaultThresholdSummary;
        width = defaultWidth;
        height = defaultHeight;
    }

    public int getMaxNoOfThreads() {
        return maxNoOfThreads;
    }

    public void setMaxNoOfThreads(int maxNoOfThreads) {
        if(isMaxNoOfThreadsConstrained(maxNoOfThreads)) {
            this.maxNoOfThreads = maxNoOfThreads;
        }
    }

    public int getThresholdSummary() {
        return thresholdSummary;
    }

    public void setThresholdSummary(int thresholdSummary) {
        if(isThresholdConstrained(thresholdSummary)) {
            this.thresholdSummary = thresholdSummary;
        }
    }

    public int getWidth() {
        return width;
    }

    public void setWidth(int width) {
        if(isWidthConstrained(width)) {
            this.width = width;
        }
    }

    public int getHeight() {
        return height;
    }

    public void setHeight(int height) {
        if(isHeightConstrained(height)) {
            this.height = height;
        }
    }

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

    private boolean isThresholdConstrained(int thresholdSummary){
        return thresholdSummary > 0 && thresholdSummary <=20;
    }

    private boolean isMaxNoOfThreadsConstrained(int maxNoOfThreads){
        return maxNoOfThreads > 0 && maxNoOfThreads <=20;
    }

    private boolean isWidthConstrained(int width){
        return width > 0 && width <= 1920;
    }

    private boolean isHeightConstrained(int height){
        return height > 0 && height <= 1080;
    }

    private boolean isConstrained(){
        return isThresholdConstrained(thresholdSummary) && isMaxNoOfThreadsConstrained(maxNoOfThreads)
                && isWidthConstrained(width) && isHeightConstrained(height);
    }

}
