package backend.system;

import edu.stanford.nlp.pipeline.StanfordCoreNLP;
import edu.stanford.nlp.util.PropertiesUtils;

/**
 * Holds all the data needed by the entire Back-end: the StanfordCoreNLP used to process text, the System state, etc.
 * Follows a Singleton design pattern, as there should be just one backend.system.BackEndSystem during the entire lifetime of the
 * system, and allows to hold data needed in different areas to be held at one place accessible to all areas.
 * Before the Object is created the backend.system.SystemState is NOT_STARTED, it moves to STARTED when it is created.
 */
public class BackEndSystem {
    private static BackEndSystem ourInstance = new BackEndSystem();
    private StanfordCoreNLP coreNLP;
    private SystemState systemState = SystemState.NOT_STARTED;

    /**
     * Will make a new backend.system.BackEndSystem if one has not been created yet, or else return the one that was previously
     * created.
     *
     * @return a backend.system.BackEndSystem that was just created or was previously created.
     */
    public static BackEndSystem getInstance() {
        return ourInstance;
    }

    /**
     * Initialises the StanfordCoreNLP and sets the backend.system.SystemState to STARTED.
     */
    private BackEndSystem() {
        coreNLP = new StanfordCoreNLP(PropertiesUtils.asProperties(
                "annotators",
                "tokenize,ssplit,pos,lemma,ner,entitymentions,parse,dcoref",
                "tokenize.language", "en"
        ));
        systemState = SystemState.STARTED;
    }

    /**
     * Get a reference to the StandfordCoreNLP that has already been loaded with all the models.
     *
     * @return a StanfordCoreNLP with all the models loaded.
     */
    public StanfordCoreNLP getCoreNLP() {
        return coreNLP;
    }

    /**
     * Get the current state of the System according to the backend.system.SystemState enum.
     *
     * @return the current state of the System.
     */
    public SystemState getSystemState() {
        return systemState;
    }

    /**
     * Update the current state of the System, i.e. when the we are beginning to process Files the backend.system.SystemState should be
     * set to PROCESSING.
     *
     * @param systemState the state to which the System should be set to.
     */
    public void setSystemState(SystemState systemState) {
        this.systemState = systemState;
        System.out.println("System is now in state: " + systemState);
    }
}
