package backend.process;

/**
 * Created by Oliver on 14/01/2017.
 */
public class FileData {
    private String fileName;
    private String filePath;

    public FileData(String fileName, String filePath){
        this.fileName = fileName;
        this.filePath = filePath;
    }

    public String getFileName() {
        return fileName;
    }

    public void setFileName(String fileName) {
        this.fileName = fileName;
    }

    public String getFilePath() {
        return filePath;
    }

    public void setFilePath(String filePath) {
        this.filePath = filePath;
    }

    @Override
    public String toString() {
        return String.format("Name: %s, Path: %s", fileName, filePath);
    }
}
