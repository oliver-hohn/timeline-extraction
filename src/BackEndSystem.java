import edu.stanford.nlp.pipeline.StanfordCoreNLP;
import edu.stanford.nlp.util.PropertiesUtils;

/**
 * Holds all the date needed by the entire Back-end.
 */
//TODO: documentation
public class BackEndSystem {
    private static BackEndSystem ourInstance = new BackEndSystem();
    private StanfordCoreNLP coreNLP;
    private SystemState systemState = SystemState.NOT_STARTED;

    public static BackEndSystem getInstance() {
        return ourInstance;
    }

    private BackEndSystem() {
        coreNLP = new StanfordCoreNLP(PropertiesUtils.asProperties(
                "annotators",
                "tokenize,ssplit,pos,lemma,ner,entitymentions,parse,dcoref",
                "tokenize.language", "en"
        ));
        systemState = SystemState.STARTED;
    }

    public StanfordCoreNLP getCoreNLP() {
        return coreNLP;
    }

    public SystemState getSystemState() {
        return systemState;
    }

    public void setSystemState(SystemState systemState) {
        this.systemState = systemState;
        System.out.println("System is now in state: "+systemState);
    }
}
