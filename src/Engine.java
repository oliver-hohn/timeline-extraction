import edu.stanford.nlp.ling.CoreAnnotations;
import edu.stanford.nlp.ling.Label;
import edu.stanford.nlp.pipeline.Annotation;
import edu.stanford.nlp.pipeline.StanfordCoreNLP;
import edu.stanford.nlp.semgraph.SemanticGraph;
import edu.stanford.nlp.semgraph.SemanticGraphCoreAnnotations;
import edu.stanford.nlp.semgraph.SemanticGraphEdge;
import edu.stanford.nlp.trees.Tree;
import edu.stanford.nlp.trees.TreeCoreAnnotations;
import edu.stanford.nlp.util.CoreMap;
import edu.stanford.nlp.util.Pair;
import edu.stanford.nlp.util.PropertiesUtils;

import java.util.ArrayList;
import java.util.List;

/**
 * Engine class that text in text as input and produces a list of Result objects, which are events depicted in the text. An event is only picked out, if it has a date
 * associated with it.
 * <p>
 * Uses the algorithm proposed in: Bonnie Dorr, David Zajic and Richard Schwartz. Hedge Trimmer: A Parse-and-Trim Approach to Headline
 * Generation. Proceedings of the HLT-NAACL 03 on Text summarization Workshop-Volume 5. Association for
 * Computational Linguistics, pp. 1–8.
 */
public class Engine {
    private static int threshold = 10;
    private StanfordCoreNLP coreNLP;

    /**
     * Set up the StanfordCoreNLP to analyze text.
     */
    public Engine() {
        coreNLP = new StanfordCoreNLP(PropertiesUtils.asProperties(
                "annotators",
                "tokenize,ssplit,pos,lemma,ner,entitymentions,parse,dcoref",
                "tokenize.language", "en"
        ));
    }

    /**
     * Produces a list of Results based on the text passed in. (Determine events in the text).
     *
     * @param input The text for which we want to produce events for.
     * @param date  The base date, from which we can determine exact dates from relative dates (eg Yesterday).
     * @return list of Results produced from events depicted in the text passed in, using the base date.
     */
    public ArrayList<Result> getResults(String input, String date) {
        ArrayList<Result> results = new ArrayList<>();

        Annotation annotation;
        annotation = new Annotation(input);
        annotation.set(CoreAnnotations.DocDateAnnotation.class, date);//setting a reference so that when it finds a normalazied entity tag that isnt complete will determine it
        coreNLP.annotate(annotation);
        //coreNLP.prettyPrint(annotation, new PrintWriter(System.out));

        for (CoreMap sentence : annotation.get(CoreAnnotations.SentencesAnnotation.class)) {
            Result result = getResult(sentence);
            if (result != null) {
                results.add(result);
            }
        }
        return results;
    }

    /**
     * Returns a Result object if the sentence contains a date, else returns null.
     * The Result object will include a list of subjects as well as a summary (or the entire text, depending on the
     * length of text) of the sentence.
     *
     * @param sentence the CoreMap that contains the Sentence we want to produce a Result for (if it has a date)
     * @return a Result object if the sentence contained a Date; null instead.
     */
    private Result getResult(CoreMap sentence) {
        Result result = new Result();
        setDatesAndSubjectsNET(sentence, result);

        if (result.getDates().size() > 0) {//we have found dates, so lets find more subjects and the event of the sentence
            //setGrammaticalSubjects(sentence,result);//setting grammatical subjects in the result object
            setEvent(sentence, result);//set the summarized sentence as the event depicted in the sentence
            return result;
        }
        return null;
    }

    /**
     * Set the Dates and Subjects for the Result based on Named-Entity Tags from the CoreMap passed in.
     * Dates have a DATE Named-Entity Tag. Subjects are LOCATIONs, ORGANIZATIONs, PERSONs, and MONEY.
     *
     * @param sentence a CoreMap that holds the sentence we want to extract data from.
     * @param result   the Result object that we are determining the data for.
     */
    private void setDatesAndSubjectsNET(CoreMap sentence, Result result) {
        for (CoreMap mention : sentence.get(CoreAnnotations.MentionsAnnotation.class)) {
            String namedEntityTag = mention.get(CoreAnnotations.NamedEntityTagAnnotation.class);
            if (namedEntityTag.equals("DATE")) {
                //found a date for the result object
                System.out.println("About to print time for the sentence: " + sentence);
                System.out.println("Normalized entity tag: " + mention.get(CoreAnnotations.NormalizedNamedEntityTagAnnotation.class));
                String date = mention.get(CoreAnnotations.TextAnnotation.class);
                System.out.println("We are storing date: " + date);
                result.addDate(date);
            } else if (namedEntityTag.equals("LOCATION") || namedEntityTag.equals("ORGANIZATION") ||
                    namedEntityTag.equals("PERSON") || namedEntityTag.equals("MONEY")) {
                //found a subject for the result object
                String subject = mention.get(CoreAnnotations.TextAnnotation.class);
                result.addSubject(subject);
            }
        }
    }

    /**
     * Finds the subjects of the sentence based on grammatical structure.
     * Gets the Basic Dependencies in the sentence and for each relation that contains the name "subj" (eg nsubj),
     * store its dependent in the Result object, this is the grammatical subject of a sentence.
     *
     * @param sentence a CoreMap that holds the sentence we want to extract data from.
     * @param result   the Result object that we are determining the grammatical subject for.
     */
    private void setGrammaticalSubjects(CoreMap sentence, Result result) {
        SemanticGraph semanticGraph = sentence.get(SemanticGraphCoreAnnotations.BasicDependenciesAnnotation.class);
        for (SemanticGraphEdge semanticGraphEdge : semanticGraph.edgeIterable()) {
            if (semanticGraphEdge.getRelation().getShortName().contains("subj")) {
                result.addSubject(semanticGraphEdge.getDependent().value());
            }
        }
    }

    /**
     * For the sentence passed in, summarize it and set it as the event of the Result object.
     * <p>
     * Following the algorithm proposed in: Bonnie Dorr, David Zajic and Richard Schwartz. Hedge Trimmer: A
     * Parse-and-Trim Approach to Headline Generation. Proceedings of the HLT-NAACL 03 on Text summarization
     * Workshop-Volume 5. Association for Computational Linguistics, pp. 1–8.
     *
     * @param sentence a CoreMap that holds the sentence we want to summarize to get the event for.
     */
    private void setEvent(CoreMap sentence, Result result) {
        Tree tree = sentence.get(TreeCoreAnnotations.TreeAnnotation.class);
        tree = getLeftmostLowestS(tree);//get leftmost-lowest S
        //remove time expressions
        removeTimeExpressions(tree, result);
        removeDeterminers(tree);//remove determiners
        xpOverXP(tree);//apply XP-over-XP rule
        xpBeforeNP(tree);//apply removal of XPs before NP rule
        cleanUp(tree);//remove any punctuation that could be left over
        tree = lastShorten(tree);//shorten the tree with the last two rules, removePPs and removeSBARs
        String event = produceString(tree);
        result.setEvent(event);
    }

    /**
     * Get the LeftmostLowest S subtree or the root of the tree if it doesn't exist.
     *
     * @param tree the Grammatical Structure of the sentence we are summarizing.
     * @return the leftmost-lowest S found in the passed in tree, can be the same tree.
     */
    private Tree getLeftmostLowestS(Tree tree) {
        //post-order to find first S
        Tree firstS = null;
        for (Tree node : tree.postOrderNodeList()) {
            if (!node.isLeaf() && node.value().equals("S")) {
                firstS = node;
                break;
            }
        }

        //if we haven't found a lowest S, then we use the root of the tree; else we just return the lowest S
        return (firstS == null) ? tree : firstS;
    }

    /**
     * Remove determiners like 'a' and 'the' from the tree.
     * Determiners are always the first child of their parent.
     *
     * @param tree the Grammatical Structure of the sentence we are summarizing.
     */
    private void removeDeterminers(Tree tree) {
        for (Tree node : tree.preOrderNodeList()) {//loop over in pre order
            if (!node.isLeaf()) {//if we have children
                Tree firstChild = node.children()[0];
                if (!firstChild.isLeaf() && firstChild.value().equals("DT")) {//if it has children and it has the determiner tag
                    Tree determiner = firstChild.children()[0];//determiners only have one child
                    if (determiner.value().equals("a") || determiner.value().equals("the")) {//if our determiner is of the type
                        node.removeChild(0);    // 'a' or 'the' then we delete it, remove the DT node that contains the 'a' or 'the'
                    }
                }
            }
        }
    }

    /**
     * Go through the tree in pre-order, finding the last appearance of an xp over an xp (so check for np-np,vp-vp,and s-s),
     * then remove all children of the outer xp, except for the first first child.
     * Repeat if we are still above the threshold.
     *
     * @param tree the Grammatical Structure of the sentence we are summmarizing
     */
    private void xpOverXP(Tree tree) {
        if (tree.yield(new ArrayList<Label>()).size() > threshold) {//if we are above threshold, we need to reduce tree
            Tree toRemoveChild = null;
            for (Tree node : tree.preOrderNodeList()) {//need to loop over tree
                if (!node.isLeaf() && node.children().length > 1) {//if found a node
                    Tree possibleXpNode = node.children()[0];//who is of value XP
                    if (node.value().equals(possibleXpNode.value()) && // and its first child is also XP, then need to
                            ((node.value().equals("NP") && possibleXpNode.value().equals("NP")) //record it to delete its
                                    || (node.value().equals("VP") && possibleXpNode.value().equals("VP")) //other children
                                    || (node.value().equals("S") && possibleXpNode.value().equals("S")))) {
                        toRemoveChild = node;
                    }
                }
            }
            if (toRemoveChild != null) {//we have a node to delete all its children except first from
                while (toRemoveChild.children().length > 1) {
                    toRemoveChild.removeChild(1);
                }
                xpOverXP(tree);
            }
        }
    }

    /**
     * If the tree size is greater than the threshold, then delete any XP (PP,NP,VP) before the subject of the sentence
     * which is the NP child of S.
     * Find NP subject of S, then search through the tree in pre order:
     * if we reach the NP of S then stop
     * if we reach an XP that isn't the NP of S (the subject of the sentence), then store its parent and the child index
     * that it is in (in the parents children list), to later remove it from the tree.
     *
     * @param tree Grammatical structure of the sentence we are summarizing.
     */
    private void xpBeforeNP(Tree tree) {
        if (tree.yield(new ArrayList<Label>()).size() > threshold && tree.value().equals("S")) {
            Tree[] childrenOfS = tree.children();
            Tree pointsToFirstNP = null;
            for (int i = 0; i < childrenOfS.length; i++) {
                Tree child = childrenOfS[i];
                if (child.value().equals("NP")) {//found subject of sentence
                    pointsToFirstNP = child;//remember it
                    break;//no point in continue to loop, just wanted to find the NP child of S
                }
            }
            if (pointsToFirstNP != null) {
                Tree toDeleteChild = null;//hold parent of node we have to delete
                int childIndexToDelete = 0;//hold index of child of parent that we have to delete
                for (Tree node : tree.preOrderNodeList()) {
                    if (node == pointsToFirstNP || toDeleteChild != null) {
                        break;
                    } else if (!node.isLeaf()) {
                        Tree[] children = node.children();
                        for (int i = 0; i < children.length; i++) {
                            Tree child = children[i];
                            if (child == pointsToFirstNP) {
                                break;
                            } else if (child.value().equals("VP") || child.value().equals("NP") || child.value().equals("PP")) {
                                toDeleteChild = node;
                                childIndexToDelete = i;
                            }
                        }
                    }
                }
                if (toDeleteChild != null) {
                    toDeleteChild.removeChild(childIndexToDelete);
                    //xpBeforeNP(tree); ? recursively call
                }
            }
        }
    }

    /**
     * Applies the last two rules for iterative shortening. First will try to remove the rightmost-lowest PP until it
     * has reached the threshold. If it has removed all the PPs, or it can't delete anymore and it is still below the
     * threshold, then undo the changes and do the below.
     * Remove the SBARs until we are below the threshold, or there are no more left. If we are still above the threshold,
     * then delete all the PPs until we are below the threshold, or there are no more left.
     * <p>
     * According to the algorithm proposed in: Bonnie Dorr, David Zajic and Richard Schwartz. Hedge Trimmer: A Parse-and-Trim Approach to Headline
     * Generation. Proceedings of the HLT-NAACL 03 on Text summarization Workshop-Volume 5. Association for
     * Computational Linguistics, pp. 1–8.
     *
     * @return a tree that has gone through the shortening of size smaller than threshold, or a tree where nothing can be removed
     */
    private Tree lastShorten(Tree tree) {
        if (tree.yield(new ArrayList<Label>()).size() > threshold) {
            Tree copyOfTree = tree.deepCopy();
            //removePPs(copyOfTree);
            removeXs(tree, "PP");
            if (copyOfTree.yield(new ArrayList<Label>()).size() > threshold) {//we undo what we did previously and do SBAR removal and then PP
                //removeSBARs(tree);//need to remove SBARs
                removeXs(tree, "SBAR");
                //removePPs(tree);//then remove PPs according to rule
                removeXs(tree, "PP");
            } else {//just the PP removal was enough to reduce the size below the threshold so we return the tree which has undergone that removal
                return copyOfTree;
            }
        }
        return tree;
    }

    /**
     * Removes trailing Xs if we are below the threshold.
     *
     * @param tree the Tree that holds the grammatical structure of the text we are summarizing.
     * @param x    the type of X we are removing, eg: PP, SBAR, from the tree.
     */
    private void removeXs(Tree tree, String x) {
        if (tree.yield(new ArrayList<Label>()).size() > threshold) {
            Tree toDeleteChild = null;
            int childIndexToDelete = 0;
            for (Tree node : tree.preOrderNodeList()) {
                if (!node.isLeaf()) {
                    Tree[] children = node.children();
                    for (int i = 0; i < children.length; i++) {
                        if (children[i].value().equals(x)) {
                            toDeleteChild = node;
                            childIndexToDelete = i;
                        }
                    }
                }
            }
            if (toDeleteChild != null) {
                toDeleteChild.removeChild(childIndexToDelete);
                removeXs(tree, x);
            }
        }
    }

    /**
     * Produce a string based on the leaf nodes in the tree.
     *
     * @param tree the Tree that has the grammatical structure of the sentence we want to produce.
     * @return the String that follows the grammatical structure passed in.
     */
    private String produceString(Tree tree) {
        String toReturn = "";
        List<Tree> preOrderList = tree.preOrderNodeList();
        for (int i = 0; i < preOrderList.size(); i++) {
            Tree node = preOrderList.get(i);
            if (node.isLeaf()) {
                toReturn += node.value();
                if (i != preOrderList.size() - 1) {
                    toReturn += " ";
                }
            }
        }
        return toReturn;
    }

    /**
     * Called to remove punctuation that is at the start of the tree
     *
     * @param tree the Tree from which we are removing the punctuation
     */
    private void cleanUp(Tree tree) {//if its the first character, than its the first child of root S
        if (!tree.isLeaf() && tree.value().equals("S") && tree.children()[0].value().equals(",")) {
            tree.removeChild(0);
        }
    }

    /**
     * Remove time expressions in the tree.
     * According to the algorithm proposed in: Bonnie Dorr, David Zajic and Richard Schwartz. Hedge Trimmer: A Parse-and-Trim Approach to Headline
     * Generation. Proceedings of the HLT-NAACL 03 on Text summarization Workshop-Volume 5. Association for
     * Computational Linguistics, pp. 1–8.
     *
     * @param tree   the Tree that contains the Grammatical Structure of the sentence we are summarizing.
     * @param result the Result object that contains previously found time expressions in the original sentence.
     */
    private void removeTimeExpressions(Tree tree, Result result) {
        /*go over the tree, for every PP check if it has a NP child, if so check if that NP contains the time expression, if so delete the PP.
        * afterwards delete any NP that contains the time expression
        * */
        System.out.println("Tree going in:");
        tree.pennPrint();
        ArrayList<Pair<Tree, Integer>> toDeleteNodes = new ArrayList<>();
        for (Tree node : tree.preOrderNodeList()) {
            if (!node.isLeaf()) {
                Tree[] children = node.children();
                for (int i = 0; i < children.length; i++) {
                    if (children[i].value().equals("PP") && !children[i].isLeaf()) {
                        Tree[] childOfChildren = children[i].children();
                        for (int j = 0; j < childOfChildren.length; j++) {
                            if (childOfChildren[j].value().equals("NP") && !childOfChildren[j].isLeaf()) {
                                String childOfChildrenString = produceString(childOfChildren[j]);
                                if (result.hasDate(childOfChildrenString)) {
                                    System.out.println("Found a match");
                                    toDeleteNodes.add(new Pair<>(node, i));
                                    break;
                                }
                            }
                        }
                    }
                }
            }
        }
        //now delete
        for (Pair<Tree, Integer> pair : toDeleteNodes) {
            if (pair.first.getChild(pair.second).value().equals("PP")) {
                System.out.println("Removing: " + pair.first.getChild(pair.second));
                pair.first.removeChild(pair.second);
            }
        }
        //tree.pennPrint();
        //clear toDeleteNodes
        toDeleteNodes.clear();
        //now find any NP with the time expression
        for (Tree node : tree.preOrderNodeList()) {
            if (!node.isLeaf()) {
                Tree[] children = node.children();
                for (int i = 0; i < children.length; i++) {
                    if (children[i].value().equals("NP") && !children[i].isLeaf()) {
                        String childString = produceString(children[i]);
                        if (result.hasDate(childString)) {
                            System.out.println("Found a match");
                            toDeleteNodes.add(new Pair<>(node, i));
                        }
                    }
                }
            }
        }
        for (Pair<Tree, Integer> pair : toDeleteNodes) {
            if (pair.first.getChild(pair.second).value().equals("NP")) {
                System.out.println("Removing: " + pair.first.getChild(pair.second));
                pair.first.removeChild(pair.second);
            }
        }
        System.out.println("Tree afterwards:");
        tree.pennPrint();

    }

}
