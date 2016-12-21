import edu.stanford.nlp.pipeline.StanfordCoreNLP;
import edu.stanford.nlp.util.PropertiesUtils;

/**
 * Holds all the date needed by the entire Back-end.
 */
public class BackEndSystem {
    private static BackEndSystem ourInstance = new BackEndSystem();
    private StanfordCoreNLP coreNLP;

    public static BackEndSystem getInstance() {
        return ourInstance;
    }

    private BackEndSystem() {
        coreNLP = new StanfordCoreNLP(PropertiesUtils.asProperties(
                "annotators",
                "tokenize,ssplit,pos,lemma,ner,entitymentions,parse,dcoref",
                "tokenize.language", "en"
        ));
    }

    public StanfordCoreNLP getCoreNLP() {
        return coreNLP;
    }
}
