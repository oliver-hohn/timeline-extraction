package backend.process;

/**
 * Class that holds the relevant data (for this project) of a File. In this case, the File's name and path in the System.
 */
public class FileData {
    private String fileName;
    private String filePath;

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


    @Override
    public String toString() {
        return String.format("Name: %s, Path: %s", fileName, filePath);
    }

    @Override
    public boolean equals(Object obj) {
        try {
            FileData otherFileData = (FileData) obj;
            return fileName.equals(otherFileData.fileName) && filePath.equals(otherFileData.filePath);
        }catch (Exception e){
            return false;
        }
    }
}
