package backend.system;


import javax.json.Json;
import javax.json.JsonBuilderFactory;
import javax.json.JsonObject;
import javax.json.JsonReader;
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
    //actual values

    private int maxNoOfThreads;
    private int thresholdSummary;
    private int width;
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
            JsonReader jsonReader = Json.createReader(new StringReader(text));
            JsonObject jsonObject = jsonReader.readObject();
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
        this.maxNoOfThreads = jsonObject.getInt("maxNoOfThreads", defaultMaxNoThreads);
        this.thresholdSummary = jsonObject.getInt("thresholdSummary", defaultThresholdSummary);
        this.width = jsonObject.getInt("width", defaultWidth);
        this.height = jsonObject.getInt("height", defaultHeight);
    }



    public boolean saveSettingsFile(){
        JsonBuilderFactory jsonBuilderFactory = Json.createBuilderFactory(null);
        JsonObject jsonObject = jsonBuilderFactory.createObjectBuilder()
                .add("maxNoOfThreads", maxNoOfThreads)
                .add("thresholdSummary", thresholdSummary)
                .add("width", width)
                .add("height", height)
                .build();
        String jsonString = jsonObject.toString();
        System.out.println("Writing: "+jsonString);
        //save to settings.ini file
        try {
            saveToSettingsFile(jsonString);
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
