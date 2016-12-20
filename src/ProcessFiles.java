import java.io.File;
import java.util.ArrayList;
import java.util.Random;
import java.util.concurrent.Semaphore;

/**
 * Handles the parsing of files, and the multi-threading of the Engine.
 */
public class ProcessFiles implements ProcessFileCallback{
    private static int maxNoOfThreads = 2;
    private Semaphore semaphore = new Semaphore(maxNoOfThreads);
    private ArrayList<Result> results = new ArrayList<>();

    public void processFiles(ArrayList<File> files){
        //for each file in the list
        //initalise a semaphore using maxnoofthreads
        System.out.println("In Thread: "+Thread.currentThread().toString());
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
        //add the results to the list held
        this.results.addAll(results);
        //release semaphore
        System.out.println("Released semaphore from Thread: "+Thread.currentThread().toString());
        semaphore.release();
    }

    private static class ProcessFile extends Thread{
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
            //check file exists in system
            if(fileExists(file)){
                //get the text for that file

                getText(file);
                //run engine on this
            }
            //set results to its result
            processFileCallback.callBack(new ArrayList<>());
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
                    case "pdf":
                        //call method to get string from pdf
                        getTextPDF(file);
                        break;
                    case "txt":
                        //call method to get string from txt
                        getTextTXT(file);
                        break;
                    default:
                        //get string from default process (open file and use buffered reader)
                        break;
                }
            }
            return "";
        }

        //TODO
        private String getTextPDF(File file){
            return "";
        }

        //TODO
        private String getTextTXT(File file){
            return "";
        }

    }
}
