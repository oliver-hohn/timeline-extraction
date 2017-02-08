package backend.process;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.attribute.BasicFileAttributes;
import java.nio.file.attribute.FileTime;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.util.Date;

/**
 * Class that holds the relevant data (for this project) of a File. In this case, the File's name and path in the System.
 */
public class FileData implements Comparable<FileData> {
    private final static SimpleDateFormat inputSimpleDateFormat = new SimpleDateFormat("dd-MM-yyyy");
    private final static SimpleDateFormat outputSimpleDateFormat = new SimpleDateFormat("yyyy-MM-dd");
    private final static String epochDateFormatted = "1970-01-01";
    private String fileName;
    private String filePath;
    private Date creationDate;

    /**
     * Used to create a FileData object, by providing its name and path.
     *
     * @param fileName the name of the File.
     * @param filePath the path of the File.
     */
    public FileData(String fileName, String filePath) {
        this.fileName = fileName;
        this.filePath = filePath;
    }

    public FileData(File file) {
        if (file.exists() && file.isFile()) {
            this.fileName = file.getName();
            this.filePath = file.getAbsolutePath();
            this.creationDate = getCreationDate(file);//set the creation date for this file
        }
    }

    /**
     * Get the name of the File that produced this FileData.
     *
     * @return the name of the File.
     */
    public String getFileName() {
        return fileName;
    }

    /**
     * Set the name of the File that produced this FileData.
     *
     * @param fileName the name of the File.
     */
    public void setFileName(String fileName) {
        this.fileName = fileName;
    }

    /**
     * Get the path of the File that produced this FileData.
     *
     * @return the path of the File.
     */
    public String getFilePath() {
        return filePath;
    }

    /**
     * Set the path of the File that produced this FileData.
     *
     * @param filePath the path of the File.
     */
    public void setFilePath(String filePath) {
        this.filePath = filePath;
    }


    /**
     * Print the information of this FileData.
     *
     * @return a String with the name and path of the File being represented.
     */
    @Override
    public String toString() {
        return String.format("Name: %s, Path: %s", fileName, filePath);
    }

    /**
     * Checks whether the given input is equal to this object.
     *
     * @param obj the Object to check equality with this object.
     * @return true if obj is a FileData object with the same data as this object; false otherwise.
     */
    @Override
    public boolean equals(Object obj) {
        try {
            FileData otherFileData = (FileData) obj;
            return fileName.equals(otherFileData.fileName) && filePath.equals(otherFileData.filePath) && creationDate.equals(otherFileData.creationDate);
        } catch (Exception e) {
            return false;
        }
    }

    /**
     * The creationDate (or baseDate) of the File this is representing.
     *
     * @return the creationDate (or baseDate) of the File as a Date object.
     */
    public Date getCreationDate() {
        return creationDate;
    }

    /**
     * Set the creation date as a String for the File this FileData is representing.
     *
     * @param creationDate the creation date as a String input of type: dd-MM-yyyy.
     */
    public void setCreationDate(String creationDate) {
        System.out.println("Set Creation Date: " + creationDate);
        try {
            this.creationDate = inputSimpleDateFormat.parse(creationDate);
        } catch (ParseException e) {
            e.printStackTrace();
        }
    }

    /**
     * Get the creation date in the format of day, followed by month, followed by year.
     *
     * @return the creation date in the format: dd-MM-yyyy
     */
    public String getCreationDateFormattedDayMonthYear() {
        return (creationDate != null) ? inputSimpleDateFormat.format(creationDate) : null;
    }

    /**
     * Get the creation date in the format of year, followed by month, followed by day.
     *
     * @return the creation date in the format: yyyy-MM-dd.
     */
    public String getCreationDateFormattedYearMonthDay() {
        return outputSimpleDateFormat.format(creationDate);
    }

    /**
     * Used to get the creation date of a input File, by looking at the attributes associated with the File.
     *
     * @param file the input File.
     * @return the creation date, or the current date now if no creation date is available, as a Date object.
     */
    private static Date getCreationDate(File file) {
        String creationDate = LocalDate.now().toString();//LocalDate.now gives you the Date for the current moment (default), in the format yyyy-MM-dd
        Date toReturn = null;
        //try and get file creation date
        Path filePath = Paths.get(file.getAbsolutePath());//only way to use BasicFileAttributes is to pass in a Path,
        try {//so get the Path for the passed in File
            BasicFileAttributes basicFileAttributes = Files.readAttributes(filePath, BasicFileAttributes.class);//creation date of a File is a basic file attribute
            FileTime creationTime = basicFileAttributes.creationTime();//can be epoch time if it does not exist, don't set it in that case
            String possibleDate = inputSimpleDateFormat.format(creationTime.toMillis());//need to check its not epoch time, as else the creation is not valid for this file
            if (!possibleDate.equals(epochDateFormatted)) {//epoch time date is given if it cant find a creation date
                creationDate = possibleDate;
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        try {
            toReturn = inputSimpleDateFormat.parse(creationDate);
        } catch (ParseException e) {
            e.printStackTrace();
        }
        return toReturn;
    }


    /**
     * Used to compare a given FileData to this FileData, by looking at their filename (to sort by their name, in
     * ascending order).
     *
     * @param o the other FileData object we are comparing to.
     * @return the result of comparing the file name of this FileData with the filename of the other FileData.
     */
    @Override
    public int compareTo(FileData o) {
        return fileName.compareTo(o.fileName);
    }
}
