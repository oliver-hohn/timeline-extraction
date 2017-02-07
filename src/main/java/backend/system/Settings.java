package backend.system;


import com.google.gson.*;
import com.google.gson.annotations.Expose;

import java.io.*;
import java.net.URISyntaxException;
import java.util.Scanner;

/**
 * Class that represents the possible Settings of the System.
 */
public class Settings implements Cloneable{
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
    @Expose (deserialize = false)
    private int maxNoOfThreads;
    @Expose (deserialize = false)
    private int thresholdSummary;
    @Expose (deserialize = false)
    private int width;
    @Expose (deserialize = false)
    private int height;

    public Settings(){
        //try to read from settings file, if not possible, then use default values
        if(!loadSettingsFile()) {
            //all the default values are used
            maxNoOfThreads = defaultMaxNoThreads;
            thresholdSummary = defaultThresholdSummary;
            width = defaultWidth;
            height = defaultHeight;
        }
    }

    private boolean loadSettingsFile(){
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
        while (scanner.hasNext()){
            stringBuilder.append(scanner.nextLine());
        }
        scanner.close();
        return stringBuilder.toString();
    }

    private void setValues(JsonObject jsonObject){
        maxNoOfThreads = (jsonObject.get(threadTag) != null) ? jsonObject.get(threadTag).getAsInt() : defaultMaxNoThreads;
        thresholdSummary = (jsonObject.get(thresholdTag) != null) ? jsonObject.get(thresholdTag).getAsInt() : defaultThresholdSummary;
        width = (jsonObject.get(widthTag) != null) ? jsonObject.get(widthTag).getAsInt() : defaultWidth;
        height = (jsonObject.get(heightTag) != null) ? jsonObject.get(heightTag).getAsInt() : defaultHeight;
    }



    public boolean saveSettingsFile(){
        final GsonBuilder gsonBuilder = new GsonBuilder();
        gsonBuilder.excludeFieldsWithoutExposeAnnotation();
        final Gson gson = gsonBuilder.create();
        try {
            String json = gson.toJson(this);
            System.out.println(json);
            saveToSettingsFile(json);
        }catch (Exception e){
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

    public int getMaxNoOfThreads() {
        return maxNoOfThreads;
    }

    public void setMaxNoOfThreads(int maxNoOfThreads) {
        this.maxNoOfThreads = maxNoOfThreads;
    }

    public int getThresholdSummary() {
        return thresholdSummary;
    }

    public void setThresholdSummary(int thresholdSummary) {
        this.thresholdSummary = thresholdSummary;
    }

    public int getWidth() {
        return width;
    }

    public void setWidth(int width) {
        this.width = width;
    }

    public int getHeight() {
        return height;
    }

    public void setHeight(int height) {
        this.height = height;
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

}
