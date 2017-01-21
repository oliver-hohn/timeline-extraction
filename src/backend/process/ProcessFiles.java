package backend.process;

import backend.system.BackEndSystem;
import backend.system.SystemState;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.text.PDFTextStripper;
import org.apache.poi.xwpf.extractor.XWPFWordExtractor;
import org.apache.poi.xwpf.usermodel.XWPFDocument;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;
import java.util.concurrent.Semaphore;


/**
 * Handles the parsing of files, and the multi-threading of the backend.process.Engine.
 */
public class ProcessFiles implements ProcessFileCallback {
    private static int maxNoOfThreads = 2;
    private Semaphore semaphore = new Semaphore(maxNoOfThreads);
    private Semaphore semaphoreFinished = new Semaphore(0);//so that we wait until all threads finish
    private ArrayList<Result> results = new ArrayList<>();
    private int filesToGo;//to notify the listener when it is done

    /**
     * For the list of Files passed in, it will process each in separate threads, while respecting the maximum number
     * of threads that can run at any given time (i.e. maximum 2 extra threads running at a time).
     * Will call gotResults() on the backend.process.CallbackResults object when all files have been processed, and a list of Results has
     * been produced.
     *
     * @param files     the list of File objects that contain text that needs to be processed (atm only processes .docx/.pdf/.txt files)
     * @param fileDatas the list of FileData objects, where the index corresponds the File in the same index in the files list, that
     *                  contains the needed data for each File.
     */
    public List<Result> processFiles(List<File> files, List<FileData> fileDatas) {
        //should only run if we are not Processing
        //this will also set up the StanfordCoreNLP (when GUI is implemented, it will already by set up, as it will be the first thing ran)
        System.out.println("Will try to run");
        if (BackEndSystem.getInstance().getSystemState() != SystemState.PROCESSING && files.size() == fileDatas.size()) {//if we arent processing, then we can begin to do that
            System.out.println("Is running");
            filesToGo = files.size();//and when we need to call
            BackEndSystem.getInstance().setSystemState(SystemState.PROCESSING);
            System.out.println("In Thread: " + Thread.currentThread().toString());
            for (int i = 0; i < files.size(); i++) {
                File file = files.get(i);
                FileData fileData = fileDatas.get(i);//should be the same
                //check they are the same?
                //acquire from the semaphore
                try {
                    System.out.println("Trying to acquire semaphore for file: " + file);
                    semaphore.acquire();//will wait if there is already maxnoofthreads running, until one finishes: then it gets to run
                    System.out.println("Acquired semaphore for file: " + file);
                    //process file
                    Thread thread = new ProcessFile(file, this, fileData);//pass a reference so that the thread can call this when it finishes processing the file
                    thread.start();//start processing this file(get its text and pass it to the backend.process.Engine)
                } catch (InterruptedException e) {
                    e.printStackTrace();
                    //should release semaphore and reduce filesToGo count if the Thread is interrupted
                }
            }
            try {
                System.out.println("Going to wait until the last files are processed.");
                semaphoreFinished.acquire();
                System.out.println("The last files have been processed.");
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            return results;
        }
        return null;
    }

    /**
     * Called when the ProcessFile Thread has finished processing a file. It is synchronized to avoid two separate threads
     * trying to add to the list of Results at the same time.
     * Enforces the rule of releasing the semaphore, as this Thread finished running, to then allow waiting Threads to run.
     * Will inform the CallbackResult object when it finishes processing all Files.
     *
     * @param results  the backend.process.Result objects produced by Processing the given file in the Thread.
     * @param fileData the data of the File that produced these Results
     */
    public synchronized void callBack(ArrayList<Result> results, FileData fileData) {
        //we finished processing a file
        filesToGo--;//one less to look at
        System.out.println("Files to go: " + filesToGo);
        //add the results to the list held
        this.results.addAll(results);
        //release semaphore
        System.out.println("Released semaphore from Thread: " + Thread.currentThread().toString());
        semaphore.release();
        //check if we have processed everything, if so release the finished semaphore
        if (filesToGo == 0) {
            //has processed
            BackEndSystem.getInstance().setSystemState(SystemState.PROCESSED);
            //has returned the results so we finished
            BackEndSystem.getInstance().setSystemState(SystemState.FINISHED);
            semaphoreFinished.release();//value is now 1, so the thread that was acquiring can continue
        }
    }

    /**
     * In charge of Processing just one File in a separate Thread.
     */
    private static class ProcessFile extends Thread {
        //Need to release even if it messes up
        File file;
        ProcessFileCallback processFileCallback;
        FileData fileData;

        /**
         * Create a ProcessFile object that holds the data needed: the File to process and who to callback when the
         * backend.process.Engine finishes processing.
         *
         * @param file                the File to process.
         * @param processFileCallback who to inform when the backend.process.Engine finished processing the given file.
         */
        ProcessFile(File file, ProcessFileCallback processFileCallback, FileData fileData) {//hold sempahore
            this.file = file;
            this.processFileCallback = processFileCallback;
            this.fileData = fileData;
        }

        /**
         * What starts running on a separate Thread. Will first get the Text for the given file, and then pass it to the
         * backend.process.Engine, which will return a list of Results that is passed to the backend.process.ProcessFileCallback.
         */
        @Override
        public void run() {
            super.run();
            System.out.println("For: " + file + " in Thread: " + Thread.currentThread().toString());//for logging purposes
            ArrayList<Result> toReturnResults = new ArrayList<>();//initially no results
            //check file exists in system
            if (fileExists(file)) {
                //fileData = new FileData(file.getName(), file.getAbsolutePath());
                //get the text for that file
                String toProcess = getText(file);//will get the text for the file considering its extension
                //run engine on this
                if (!toProcess.equals("")) {//if we actually have text to process, don't waste time attempting to process else
                    String baseDate = fileData.getCreationDateFormattedYearMonthDay();//since we will need to process, get a base date to use
                    System.out.println("Base Date for " + file.getName() + " is: " + baseDate);
                    toReturnResults = new Engine().getResults(toProcess, baseDate);//pass in file data, so each result holds it
                    addFileData(fileData, toReturnResults);
                }
            }
            //call the backend.process.ProcessFileCallback that we finished processing and return the results of processing that one File.
            processFileCallback.callBack(toReturnResults, fileData);
        }

        /**
         * For the list passed in, set for all of them the given FileData.
         *
         * @param fileData the FileData to be set to all the Results passed in.
         * @param results  the Results for which the FileData needs to be set.
         */
        private void addFileData(FileData fileData, ArrayList<Result> results) {
            for (Result result : results) {
                result.setFileData(fileData);
            }
        }

        /**
         * Check that we can actually use the given File.
         *
         * @param file the File that we can check that we can read it.
         * @return true if  it is indeed a File, that it still exists in the system, and that we can read data from it, have access for that.
         */
        private boolean fileExists(File file) {
            return file.exists() && file.isFile() && file.canRead();
        }

        /**
         * For the given File, perform the operations needed to obtain its text, as different file extension have different
         * encodings.
         *
         * @param file the File for which we need to get text from.
         * @return the text from the File, or empty text if it was not possible to get text from the File.
         */
        private String getText(File file) {
            String fileName = file.getName();//file should have format: name.extension
            int lastDotPosition = fileName.lastIndexOf(".");//to separate name from the extension
            if (lastDotPosition != -1) {//-1 if . was no where in the file name
                String fileExtension = fileName.substring(lastDotPosition);//everything after the last dot, the extension
                System.out.println("File name: " + fileName + " file extension: " + fileExtension);
                switch (fileExtension) {
                    case ".pdf"://file has a .pdf extension
                        //call method to get string from pdf
                        return getTextPDF(file);
                    case ".txt"://file has a .txt extension
                        //call method to get string from txt
                        return getTextTXT(file);
                    case ".docx":
                        return getTextDoc(file);
                    default://could hold list of files that could not be processed
                        //get string from default process (open file and use buffered reader)
                        break;
                }
            }
            return "";
        }


        /**
         * Get the text for a PDF File using the appropriate library (Apache PDFBox).
         *
         * @param file PDF File to get text from.
         * @return the text in the PDF File, or an empty String if it was not possible.
         */
        private String getTextPDF(File file) {
            String toReturn = "";//base text, if it fails we just return empty text
            try {
                PDDocument pdDocument = PDDocument.load(file);//create Document that has processed the bytes in the pdf file
                toReturn = new PDFTextStripper().getText(pdDocument);//to then get its text
                pdDocument.close();//always remember to close the stream, or document in this case
            } catch (IOException e) {
                e.printStackTrace();
            }
            //System.out.println("For file:"+file.getName()+" has text: "+toReturn);
            return toReturn;
        }

        /**
         * Get the text for a TXT File, assuming the enconding is UTF-8(later determine encoding and pass it to the Scanner
         * that processes the File).
         *
         * @param file a TXT File to get the text from.
         * @return the text in the TXT File, or an empty String if it was not possible.
         */
        private String getTextTXT(File file) {
            //should determine character set, can look at https://code.google.com/archive/p/juniversalchardet/
            String toReturn = "";//base text to return, empty String
            try {
                Scanner scanner = new Scanner(file, "UTF-8");//uses the default character set UTF-8
                while (scanner.hasNextLine()) {//while we can read more
                    toReturn += scanner.nextLine();
                }
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            }
            return toReturn;
        }

        /**
         * Process a DOCX Document using the appropriate library (Apache POI).
         *
         * @param file the DOCX File to get the text from.
         * @return the text in the File, or an empty String if it was not possible to process the File.
         */
        private String getTextDoc(File file) {
            String toReturn = "";//base text to return, empty String
            try {
                XWPFDocument xwpfDocument = new XWPFDocument(new FileInputStream(file));//used to process docx documents
                XWPFWordExtractor xwpfWordExtractor = new XWPFWordExtractor(xwpfDocument);
                toReturn = xwpfWordExtractor.getText();
            } catch (IOException e) {
                e.printStackTrace();
            }
            //System.out.println("For file:"+file.getName()+" has text: "+toReturn);
            return toReturn;
        }
    }
}
