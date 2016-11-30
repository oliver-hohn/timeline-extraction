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
 */
//TODO: remove temporal expressions before getting event, or threshold doesn't account for time expression
public class Engine {
    private static int threshold = 10;
    private StanfordCoreNLP coreNLP;
    public Engine(){
        coreNLP = new StanfordCoreNLP(PropertiesUtils.asProperties(
                "annotators",
                "tokenize,ssplit,pos,lemma,ner,entitymentions,parse,dcoref",
                "tokenize.language", "en"
        ));
    }

    public ArrayList<Result> getResults(String input, String date){
        ArrayList<Result> results = new ArrayList<>();

        Annotation annotation;
        annotation = new Annotation(input);
        annotation.set(CoreAnnotations.DocDateAnnotation.class,date);//setting a reference so that when it finds a normalazied entity tag that isnt complete will determine it
        coreNLP.annotate(annotation);
        //coreNLP.prettyPrint(annotation, new PrintWriter(System.out));

        for(CoreMap sentence: annotation.get(CoreAnnotations.SentencesAnnotation.class)){
            Result result = getResult(sentence);
            if(result != null){
                results.add(result);
            }
        }
        return results;
    }

    /**
     * Returns a Result object if the sentence contains a date, else returns null.
     * The Result object will include a list of subjects as well as a summary (or the entire text, depending on length of text)
     * of the sentence.
     * @param sentence
     * @return
     */
    private Result getResult(CoreMap sentence){
        Result result = new Result();
        setDatesAndSubjectsNET(sentence,result);
        /*Annotation annotation = new Annotation(newString);
        coreNLP.annotate(annotation);*/
        //CoreMap coreMap = annotation.get(CoreAnnotations.SentencesAnnotation.class).get(0);
        if(result.getDates().size() > 0){//we have found dates, so lets find more subjects and the event of the sentence
            //setGrammaticalSubjects(sentence,result);//setting grammatical subjects in the result object
            setEvent(sentence,result);//set the summarized sentence as the event depicted in the sentence
            return result;
        }
        return null;
    }

    /**
     * Set the Dates and Subjects for the Result object based on Named-Entity Tags from the CoreMap passed in.
     */
    private void setDatesAndSubjectsNET(CoreMap sentence, Result result){
        for(CoreMap mention: sentence.get(CoreAnnotations.MentionsAnnotation.class)) {
            String namedEntityTag = mention.get(CoreAnnotations.NamedEntityTagAnnotation.class);
            if (namedEntityTag.equals("DATE")) {
                //found a date for the result object
                System.out.println("About to print time for the sentence: "+sentence);
                System.out.println("Normalized entity tag: "+mention.get(CoreAnnotations.NormalizedNamedEntityTagAnnotation.class));
                String date = mention.get(CoreAnnotations.TextAnnotation.class);
                System.out.println("We are storing date: "+date);
                result.addDate(date);
            } else if (namedEntityTag.equals("LOCATION") || namedEntityTag.equals("ORGANIZATION") ||
                    namedEntityTag.equals("PERSON") || namedEntityTag.equals("MONEY")) {
                //found a subject for the result object
                String subject = mention.get(CoreAnnotations.TextAnnotation.class);
                result.addSubject(subject);

            }
        }
        //return actualText;
    }

    /**
     * Sets the subjects of the sentence based on grammatical structure.
     * Gets the Basic Dependencies in the sentence and for each relation that contains the name "subj" (eg nsubj),
     * store its dependent in the Result object. The is a grammatical subject of a sentence.
     * @param sentence
     * @param result
     */
    private void setGrammaticalSubjects(CoreMap sentence, Result result){
        SemanticGraph semanticGraph = sentence.get(SemanticGraphCoreAnnotations.BasicDependenciesAnnotation.class);
        for(SemanticGraphEdge semanticGraphEdge: semanticGraph.edgeIterable()){
            if(semanticGraphEdge.getRelation().getShortName().contains("subj")){
                result.addSubject(semanticGraphEdge.getDependent().value());
            }
        }
    }

    /**
     * For the sentence passed in, summarize it and set it as the event of the Result object.
     * @param sentence
     * @param result
     */
    private void setEvent(CoreMap sentence, Result result){
        Tree tree = sentence.get(TreeCoreAnnotations.TreeAnnotation.class);
        tree = getLeftmostLowestS(tree);//get leftmost-lowest S
        //remove time expressions
        removeTimeExpressions(tree,result);
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
     * @param tree
     * @return
     */
    private Tree getLeftmostLowestS(Tree tree){
        //post-order to find first S
        Tree firstS = null;
        for(Tree node: tree.postOrderNodeList()){
            if(!node.isLeaf() && node.value().equals("S")){
                firstS = node;
                break;
            }
        }

        //if we haven't found a lowest S, then we use the root of the tree; else we just return the lowest S
        return (firstS == null) ? tree : firstS;
    }

    /**
     * Remove determiners like 'a' and 'the' from the tree.
     * Determiners are always the first child in of a parent that contains it
     * @param tree
     */
    private void removeDeterminers(Tree tree){
        for(Tree node: tree.preOrderNodeList()){//loop over in pre order
            if(!node.isLeaf()){//if we have children
                Tree firstChild = node.children()[0];
                if(!firstChild.isLeaf() && firstChild.value().equals("DT")){//if it has children and it has the determiner tag
                    Tree determiner = firstChild.children()[0];//determiners only have one child
                    if(determiner.value().equals("a") || determiner.value().equals("the")){//if our determiner is of the type
                        node.removeChild(0);    // 'a' or 'the' then we delete it, remove the DT node that contains the 'a' or 'the'
                    }
                }
            }
        }
    }

    /**
     * go through preorder through the tree, finding the last one with xp xp (so check for np-np,vp-vp,and s-s)
     * then remove all children except first of that tree.
     * then check if size of leaf nodes is less than threshold, if not repeat
     * @param tree
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
            if(toRemoveChild != null){//we have a node to delete all its children except first from
                while (toRemoveChild.children().length > 1){
                    toRemoveChild.removeChild(1);
                }
                xpOverXP(tree);
            }else{//this could be remmoved as it wont call the recursive call if it doesnt enter the if-statement
                return;
            }
        }
    }

    /**
     * Delete any XP (PP,NP,VP) before the subject of the sentence which is the NP child of S, if tree size is > than threshold
     * Find NP subject of S
     * then do pre order, if we reach NP of S then stop
     * if before that we reach an XP that isnt the the NP of S, then store its parent and its index as a child
     * and remove it from the tree
     * @param tree
     */
    private void xpBeforeNP(Tree tree){
        if(tree.yield(new ArrayList<Label>()).size() > threshold && tree.value().equals("S")){
            Tree[] childrenOfS = tree.children();
            Tree pointsToFirstNP = null;
            for(int i = 0; i<childrenOfS.length; i++){
                Tree child = childrenOfS[i];
                if(child.value().equals("NP")){//found subject of sentence
                    pointsToFirstNP = child;//remember it
                    break;//no point in continue to loop, just wanted to find the NP child of S
                }
            }
            if(pointsToFirstNP != null){
                Tree toDeleteChild = null;//hold parent of node we have to delete
                int childIndexToDelete = 0;//hold index of child of parent that we have to delete
                for(Tree node: tree.preOrderNodeList()){
                    if(node == pointsToFirstNP || toDeleteChild != null){
                        break;
                    }else if(!node.isLeaf()){
                        Tree[] children = node.children();
                        for(int i=0; i<children.length; i++){
                            Tree child = children[i];
                            if(child == pointsToFirstNP){
                                break;
                            }else if(child.value().equals("VP") ||child.value().equals("NP") || child.value().equals("PP")){
                                toDeleteChild = node; childIndexToDelete = i;
                            }
                        }
                    }
                }
                if(toDeleteChild != null){
                    toDeleteChild.removeChild(childIndexToDelete);
                    //xpBeforeNP(tree); ? recursively call
                }
            }
        }
    }

    /**
     * Applies the last two rules for iterative shortening:
     * will try to remove rightmost-lowest PP until reach threshold, if still after that below threshold, then undo changes, and remove rightmost-lowest SBAR
     * until threshold is reached, if cant be reached then remove rightmost lowest PP until threshold is reached.
     * Returns a tree that has gone through the shortening of size smaller than threshold, or a tree where nothing can be removed
     * @return
     */
    private Tree lastShorten(Tree tree){
        if(tree.yield(new ArrayList<Label>()).size() > threshold){
            Tree copyOfTree = tree.deepCopy();
            removePPs(copyOfTree);
            if(copyOfTree.yield(new ArrayList<Label>()).size() > threshold){//we undo what we did previously and do SBAR removal and then PP
                removeSBARs(tree);//need to remove SBARs
                removePPs(tree);//then remove PPs according to rule
            }else{//just the PP removal was enough to reduce the size below the threshold so we return the tree which has undergone that removal
                return copyOfTree;
            }
        }
        return tree;
    }

    /**
     * Remove trailing PPs (last PPs, work back)if we are below the threshold
     * @param tree
     */
    private void removePPs(Tree tree){
        if(tree.yield(new ArrayList<Label>()).size() > threshold){
            Tree toDeleteChild = null;
            int childIndexToDelete = 0;
            for(Tree node: tree.preOrderNodeList()){
                if(!node.isLeaf()){
                    Tree[] children = node.children();
                    for(int i=0; i<children.length; i++){
                        if(children[i].value().equals("PP")){
                            toDeleteChild = node;
                            childIndexToDelete = i;
                        }
                    }
                }
            }
            if(toDeleteChild != null){
                toDeleteChild.removeChild(childIndexToDelete);
                removePPs(tree);//loop over again, will not be done again if we below threshold or we cant delete further
            }
        }
    }

    /**
     * Remove trailing SBARs if we are below the threshold
     * @param tree
     */
    private void removeSBARs(Tree tree){
        if(tree.yield(new ArrayList<Label>()).size() > threshold){
            Tree toDeleteChild = null;
            int childIndexToDelete = 0;
            for(Tree node: tree.preOrderNodeList()){
                if(!node.isLeaf()) {//if the node has children
                    Tree[] children = node.children();
                    for(int i=0; i<children.length; i++){//then check is children
                        if(children[i].value().equals("SBAR")){//if one of them is an SBAR remember the parent, last one to update will be the rightmost SBAR (as we go through
                            toDeleteChild = node;           //the tree in preorder
                            childIndexToDelete = i;
                        }
                    }
                }
            }
            if(toDeleteChild != null){
                toDeleteChild.removeChild(childIndexToDelete);
                removeSBARs(tree);//recursively call again, wont be called if we are below the threshold or we have nothing to delete
            }
        }
    }

    /**
     * Produce a string based on the leaf nodes in the tree.
     * @param tree
     * @return
     */
    private String produceString(Tree tree){
        String toReturn = "";
        List<Tree> preOrderList = tree.preOrderNodeList();
        for(int i=0; i<preOrderList.size(); i++){
            Tree node = preOrderList.get(i);
            if(node.isLeaf()) {
                toReturn += node.value();
                if(i != preOrderList.size() - 1){
                    toReturn += " ";
                }
            }
        }
        return toReturn;
    }

    /**
     * Called to remove punctuation that is at the start of the tree
     * @param tree
     */
    private void cleanUp(Tree tree){//if its the first character, than its the first child of root S
        if(!tree.isLeaf() && tree.value().equals("S") && tree.children()[0].value().equals(",")) {
            tree.removeChild(0);
        }
    }

    /**
     * Remove time expressions in the tree.
     * @param tree
     * @param result
     */
    private void removeTimeExpressions(Tree tree, Result result){
        /*go over the tree, for every PP check if it has a NP child, if so check if that NP contains the time expression, if so delete the PP.
        * afterwards delete any NP that contains the time expression
        * */
        System.out.println("Tree going in:");
        tree.pennPrint();
        ArrayList<Pair<Tree,Integer>> toDeleteNodes = new ArrayList<>();
        for(Tree node : tree.preOrderNodeList()){
            if(!node.isLeaf()){
                Tree[] children = node.children();
                for(int i=0; i<children.length; i++){
                    if(children[i].value().equals("PP") && !children[i].isLeaf()){
                        Tree[] childOfChildren = children[i].children();
                        for(int j=0; j<childOfChildren.length; j++){
                            if(childOfChildren[j].value().equals("NP") && !childOfChildren[j].isLeaf()){
                                String childOfChildrenString = produceString(childOfChildren[j]);
                                if(result.hasDate(childOfChildrenString)){
                                    System.out.println("Found a match");
                                    toDeleteNodes.add(new Pair<>(node,i));
                                    break;
                                }
                            }
                        }
                    }
                }
            }
        }
        //now delete
        for(Pair<Tree,Integer> pair : toDeleteNodes){
            if(pair.first.getChild(pair.second).value().equals("PP")){
                System.out.println("Removing: "+pair.first.getChild(pair.second));
                pair.first.removeChild(pair.second);
            }
        }
        //tree.pennPrint();
        //clear toDeleteNodes
        toDeleteNodes.clear();
        //now find any NP with the time expression
        for(Tree node: tree.preOrderNodeList()){
            if(!node.isLeaf()){
                Tree[] children = node.children();
                for(int i=0; i<children.length; i++){
                    if(children[i].value().equals("NP")&&!children[i].isLeaf()){
                        String childString = produceString(children[i]);
                        if(result.hasDate(childString)){
                            System.out.println("Found a match");
                            toDeleteNodes.add(new Pair<>(node,i));
                        }
                    }
                }
            }
        }
        for(Pair<Tree,Integer> pair : toDeleteNodes){
            if(pair.first.getChild(pair.second).value().equals("NP")){
                System.out.println("Removing: "+pair.first.getChild(pair.second));
                pair.first.removeChild(pair.second);
            }
        }
        System.out.println("Tree afterwards:");
        tree.pennPrint();

    }

}
