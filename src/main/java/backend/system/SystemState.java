package backend.system;

/**
 * Represents all the different states the Back-end (specifically the backend.process.Engine) can be in.
 */
public enum SystemState {
    NOT_STARTED,//initially when the StanfordCoreNLP has not been started
    STARTED,//when the StanfordCoreNLP has been started
    PROCESSING,//when the back-end is processing files
    PROCESSED,//when it has processed all files
    FINISHED//when it has returned the Results of processing the Files
}
