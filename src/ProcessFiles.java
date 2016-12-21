import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.text.PDFTextStripper;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.attribute.BasicFileAttributes;
import java.nio.file.attribute.FileTime;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Scanner;
import java.util.concurrent.Semaphore;

/**
 * Handles the parsing of files, and the multi-threading of the Engine.
 */
//TODO:documentation
public class ProcessFiles implements ProcessFileCallback{
    private static int maxNoOfThreads = 2;
    private Semaphore semaphore = new Semaphore(maxNoOfThreads);
    private ArrayList<Result> results = new ArrayList<>();
    private int filesToGo;//to notify the listener when it is done
    private CallbackResults callbackResults;// who to inform when we are done
    private final static SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd");
    private final static String epochDateFormatted = "1970-01-01";


    public void processFiles(ArrayList<File> files, CallbackResults callbackResults){
        this.callbackResults = callbackResults;//set up who we need to call
        filesToGo = files.size();//and when we need to call
        //for each file in the list
        //initialise a semaphore using maxnoofthreads
        System.out.println("In Thread: "+Thread.currentThread().toString());
        //start the counter for the amount of files to still process, to know when to notify the listener it is done processing

        for(File file : files){
            //acquire from the semaphore
            try {
                System.out.println("Trying to acquire semaphore for file: "+file);
                semaphore.acquire();
                System.out.println("Acquired semaphore for file: "+file);
                //process file
                Thread thread = new ProcessFile(file, this);
                thread.start();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    //gets called when the thread finishes to release the semaphore
    public synchronized void callBack(ArrayList<Result> results){
        //we finished processing a file
        filesToGo--;//one less to look at
        System.out.println("Files to go: "+filesToGo);
        //add the results to the list held
        this.results.addAll(results);
        //release semaphore
        System.out.println("Released semaphore from Thread: "+Thread.currentThread().toString());
        semaphore.release();
        //check if we have processed everything, if so inform listener
        if(filesToGo == 0 && callbackResults != null){
            callbackResults.gotResults(this.results);//return all the results obtained until now
        }
    }

    private static class ProcessFile extends Thread{
        //Need to release even if it messes up
        File file;
        ProcessFileCallback processFileCallback;
        ProcessFile(File file, ProcessFileCallback processFileCallback){//hold sempahore
            this.file = file;
            this.processFileCallback = processFileCallback;
        }

        @Override
        public void run() {
            super.run();
            System.out.println("For: "+file+" in Thread: "+Thread.currentThread().toString());
            ArrayList<Result> toReturnResults = new ArrayList<>();
            //check file exists in system
            if(fileExists(file)){
                //get the text for that file

                String toProcess  = getText(file);
                //run engine on this
                if(!toProcess.equals("")){
                    String baseDate = getFileCreationDate(file);
                    toReturnResults = new Engine().getResults(toProcess,baseDate);
                }
            }
            //set results to its result
            processFileCallback.callBack(toReturnResults);
        }

        private boolean fileExists(File file){
            return file.exists() && file.isFile() && file.canRead();
        }

        private String getText(File file){
            String fileName = file.getName();//file should have format: name.extension
            int lastDotPosition = fileName.lastIndexOf(".");//to separate name from the extension
            if(lastDotPosition != -1){//-1 if . was no where in the file name
                String fileExtension = fileName.substring(lastDotPosition);//everything after the last dot, the extension
                System.out.println("File name: "+fileName+" file extension: "+fileExtension);
                switch (fileExtension){
                    case ".pdf"://file has a .pdf extension
                        //call method to get string from pdf
                        return getTextPDF(file);
                    case ".txt"://file has a .txt extension
                        //call method to get string from txt
                        return getTextTXT(file);
                    default:
                        //get string from default process (open file and use buffered reader)
                        break;
                }
            }
            return "";
        }


        private String getTextPDF(File file)  {
            String toReturn = "";
            try {
                PDDocument pdDocument = PDDocument.load(file);
                toReturn = new PDFTextStripper().getText(pdDocument);
                pdDocument.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
            System.out.println("For file:"+file.getName()+" has text: "+toReturn);
            return toReturn;
        }

        private String getTextTXT(File file){
            //should determine character set, can look at https://code.google.com/archive/p/juniversalchardet/
            String toReturn = "";
            try {
                Scanner scanner = new Scanner(file, "UTF-8");//uses the default character set
                while (scanner.hasNextLine()){//while we can read more
                    toReturn += scanner.nextLine();
                }
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            }
            return toReturn;
        }

        private String getFileCreationDate(File file){
            String toReturn = LocalDate.now().toString();//LocalDate.now gives you the Date for the current moment (default), in the format yyyy-MM-dd
            //try and get file creation date
            Path filePath = Paths.get(file.getAbsolutePath());
            try {
                BasicFileAttributes basicFileAttributes = Files.readAttributes(filePath, BasicFileAttributes.class);
                FileTime creationTime = basicFileAttributes.creationTime();//can be epoch time if it does not exitst, dont set it in that case
                String possibleDate = simpleDateFormat.format(creationTime.toMillis());//need to check its not epoch time, as else the creation is not valid for this file
                if(!possibleDate.equals(epochDateFormatted)){
                    toReturn = possibleDate;
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
            return toReturn;
        }
    }
}
