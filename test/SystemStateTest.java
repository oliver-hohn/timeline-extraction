import org.junit.Assert;
import org.junit.Test;

import java.io.File;
import java.util.ArrayList;

/**
 * Test class to test the change of System States as files are being Processed. Checks for STARTED, PROCESSING, and PROCESSED
 * System States, as these are the three main different states.
 */
public class SystemStateTest {
    private CallbackResults callbackResults = new CallbackResults() {
        @Override
        public void gotResults(ArrayList<Result> results) {
            //we just finished processing a File, so our SystemState should be PROCESSED (FINISHED set after it passes the Results).
            System.out.println("Checking if SystemState is PROCESSED");
            Assert.assertEquals(BackEndSystem.getInstance().getSystemState(), SystemState.PROCESSED);
        }
    };

    /**
     * Checks the initial state, then when processing, and then when it should be finished.
     *
     * @throws InterruptedException as we make the Test thread wait for ProcessFile to finish processing the Files.
     */
    @Test
    public void testSystemState() throws InterruptedException {
        BackEndSystem.getInstance().setSystemState(SystemState.STARTED);//will start the system if it hasnt started before, if it has reset the SystemState
        System.out.println("Checking if SystemState is STARTED");
        Assert.assertEquals(BackEndSystem.getInstance().getSystemState(), SystemState.STARTED);//check the system is started

        ProcessFiles processFiles = new ProcessFiles();

        File testFile1 = new File("D:" + File.separator + "FYP" + File.separator + "testfile.txt");
        File testFile2 = new File("D:" + File.separator + "FYP" + File.separator + "testfile2.txt");
        File testFile3 = new File("D:" + File.separator + "FYP" + File.separator + "testfile3.txt");

        ArrayList<File> files = new ArrayList<>();
        files.add(testFile1);
        files.add(testFile2);
        files.add(testFile3);

        processFiles.processFiles(files, callbackResults);
        //its processing Files, so its status should be: PROCESSING
        System.out.println("Checking if SystemState is PROCESSING");
        Assert.assertEquals(BackEndSystem.getInstance().getSystemState(), SystemState.PROCESSING);
        Thread.sleep(5000);//wait for the ProcessFile to be completed
    }
}
