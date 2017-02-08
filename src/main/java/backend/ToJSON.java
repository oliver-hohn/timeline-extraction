package backend;

import backend.process.FileData;
import backend.process.Result;
import com.google.gson.*;

import java.lang.reflect.Type;
import java.util.List;

/**
 * Class used to turn a list of Results into a JSON String.
 */
public class ToJSON {
    /**
     * For the given List of Result objects, produce a JSON String of an array, where each index corresponds to one Result
     * in the list. Each Result is given by its range (date1, date2), its subjects (array of subjects), its event, and its
     * FileData (the filename, and base date used for processing the file), represented as a JsonObject from.
     *
     * @param results the given List of Result objects.
     * @return the JSON String representing the list of Result objects.
     */
    public static String toJSON(List<Result> results) {
        String toReturn;
        final GsonBuilder gsonBuilder = new GsonBuilder();
        gsonBuilder.excludeFieldsWithoutExposeAnnotation();
        gsonBuilder.registerTypeAdapter(Result.class, new JsonSerializer<Result>() {
            @Override
            public JsonElement serialize(Result src, Type typeOfSrc, JsonSerializationContext context) {
                JsonObject jsonObject = new JsonObject();
                //adding the range dates (which can be null)
                jsonObject.addProperty("date1", src.getTimelineDate().getDate1FormattedDayMonthYear());
                jsonObject.addProperty("date2", src.getTimelineDate().getDate2FormattedDayMonthYear());
                //adding the subjects
                JsonArray subjectJsonArray = new JsonArray();
                for (String subject : src.getSubjects()) {
                    subjectJsonArray.add(subject);
                }
                jsonObject.add("subjects", subjectJsonArray);
                //adding the event
                jsonObject.addProperty("event", src.getEvent());
                //adding the file data (excluding the path, since this can be used on other system where files are elsewhere)
                JsonObject fromJsonObject = new JsonObject();
                FileData fileData = src.getFileData();
                if (fileData != null) {
                    fromJsonObject.addProperty("filename", fileData.getFileName());
                    fromJsonObject.addProperty("baseDate", fileData.getCreationDateFormattedDayMonthYear());
                }
                jsonObject.add("from", fromJsonObject);

                return jsonObject;
            }
        });
        gsonBuilder.setPrettyPrinting();
        final Gson gson = gsonBuilder.create();
        toReturn = gson.toJson(results);
        return toReturn;
    }
}
