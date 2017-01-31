package backend;

import backend.process.FileData;
import backend.process.ProcessFiles;
import backend.system.BackEndSystem;
import backend.system.SystemState;
import org.junit.Assert;
import org.junit.Test;

import java.io.File;
import java.net.URISyntaxException;
import java.util.ArrayList;

/**
 * Test class to test the change of System States as files are being Processed. Checks for STARTED, PROCESSING, and PROCESSED
 * System States, as these are the three main different states.
 */
public class SystemStateTest {

    /**
     * Checks the initial state, then when processing, and then when it should be finished.
     *
     * @throws InterruptedException as we make the Test thread wait for ProcessFile to finish processing the Files.
     */
    @Test
    public void testSystemStatePROCESSING() throws InterruptedException, URISyntaxException {
        BackEndSystem.getInstance().setSystemState(SystemState.STARTED);
        Assert.assertEquals(SystemState.STARTED, BackEndSystem.getInstance().getSystemState());

        ProcessFiles processFiles = new ProcessFiles();
        File testFile = new File(getClass().getResource("testfile1.txt").toURI());
        ArrayList<File> files = new ArrayList<>();
        files.add(testFile);

        ArrayList<FileData> fileDatas = new ArrayList<>();
        for(File file: files){
            fileDatas.add(new FileData(file));
        }

        Runnable newRunnable = new Runnable() {
            @Override
            public void run() {
                processFiles.processFiles(files, fileDatas);
            }
        };

        Thread thread = new Thread(newRunnable);
        thread.start();
        Thread.sleep(100);
        System.out.println("System state: " + BackEndSystem.getInstance().getSystemState());
        Assert.assertEquals(SystemState.PROCESSING, BackEndSystem.getInstance().getSystemState());

        Thread.sleep(5000);

    }


    /**
     * Checks that the state is set to FINISHED when files have been processed, and the result has been returned.
     */
    @Test
    public void testSystemStateFINISHED() throws URISyntaxException {
        BackEndSystem.getInstance().setSystemState(SystemState.STARTED);
        Assert.assertEquals(SystemState.STARTED, BackEndSystem.getInstance().getSystemState());

        ProcessFiles processFiles = new ProcessFiles();
        File testFile = new File(getClass().getResource("testfile1.txt").toURI());
        ArrayList<File> files = new ArrayList<>();
        files.add(testFile);

        ArrayList<FileData> fileDatas = new ArrayList<>();
        for(File file: files){
            fileDatas.add(new FileData(file));
        }

        processFiles.processFiles(files, fileDatas);

        Assert.assertEquals(SystemState.FINISHED, BackEndSystem.getInstance().getSystemState());

    }
}
