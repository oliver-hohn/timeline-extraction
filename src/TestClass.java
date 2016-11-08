import edu.stanford.nlp.ling.CoreAnnotations;
import edu.stanford.nlp.ling.IndexedWord;
import edu.stanford.nlp.ling.Label;
import edu.stanford.nlp.parser.nndep.DependencyParser;
import edu.stanford.nlp.parser.nndep.demo.DependencyParserDemo;
import edu.stanford.nlp.pipeline.Annotation;
import edu.stanford.nlp.pipeline.StanfordCoreNLP;
import edu.stanford.nlp.semgraph.SemanticGraph;
import edu.stanford.nlp.semgraph.SemanticGraphCoreAnnotations;
import edu.stanford.nlp.semgraph.SemanticGraphEdge;
import edu.stanford.nlp.trees.*;
import edu.stanford.nlp.trees.Tree;
import edu.stanford.nlp.util.CoreMap;
import edu.stanford.nlp.util.Index;
import edu.stanford.nlp.util.IterableIterator;
import edu.stanford.nlp.util.PropertiesUtils;
import edu.stanford.nlp.util.logging.Redwood;

import java.io.PrintWriter;
import java.util.*;

/**
 * Created by Oliver on 29/10/2016.
 */
public class TestClass {
    private static Redwood.RedwoodChannels log = Redwood.channels(DependencyParserDemo.class);
    private static final String taggerPath = "edu/stanford/nlp/models/pos-tagger/english-left3words/english-left3words-distsim.tagger";
    private static final String modelPath = DependencyParser.DEFAULT_MODEL;

    public static void main(String[] args) {
      //  MaxentTagger maxentTagger = new MaxentTagger("edu/stanford/nlp/models/pos-tagger/english-left3words/english-left3words-distsim.tagger" );
        StanfordCoreNLP stanfordCoreNLP = new StanfordCoreNLP(PropertiesUtils.asProperties(
                "annotators",
                "tokenize,ssplit,pos,lemma,ner,entitymentions,parse,dcoref",
                "tokenize.language", "en"
        ));





       /* String text = "I can almost always tell when movies use fake dinosaurs. On the 12th of December I played football. " +
                "Yesterday Marry disappeared while running on North St, it was a cold night.";*/
/*        String text = "John, who was the CEO of a company, played golf. On the 12th of December I played Basketball with Oliver and Tom. Yesterday," +
                " I studied for 40 hours, lmao.";*/
/*
        String text = "A fire killed a firefighter who was fatally injured as he searched the house. Illegal fireworks injured hundreds of people and started six fires. ";
*/
        String text = "On Friday the Washington Post came out with the latest from its long-running investigation into Trump's charitable donations.\n" +
                "\n" +
                "In its latest story, the paper called 420-plus charities with some connection to Trump but found only one personal gift from him between 2008 and the spring of this year. \n" +
                "\n" +
                "But the Post did find nearly $8m that Trump has donated from his own pocket since the early 1980s.\n" +
                "\n" +
                "One of the bizarre episodes the paper recounts is that in 1996, Trump showed up without an invitation to a charity for the Association to Benefit Children where he took a seat on the stage that had been reserved for a major donor, despite not being a donor himself.\n" +
                "\n" +
                "In response the piece, the Trump campaign told the Post that he \"has personally donated tens of millions of dollars... to charitable causes\".";
        ArrayList<Result> results = new TestClass().results(stanfordCoreNLP,text);
        for(Result result: results){
            System.out.println(result);
        }

    }

    private ArrayList<Result> results(StanfordCoreNLP stanfordCoreNLP, String text){
        ArrayList<Result> results = new ArrayList<>();
        Annotation annotation = new Annotation(text);

        stanfordCoreNLP.annotate(annotation);
        List<CoreMap> sentences = annotation.get(CoreAnnotations.SentencesAnnotation.class);//all the sentences, that are annotated with pos and ner
        for(CoreMap sentence: sentences){//look at each sentence individually
            Result result = new Result();
            ArrayList<String> dateMentions = new ArrayList<>();
            //date will use mentions annotation
            for(CoreMap mention: sentence.get(CoreAnnotations.MentionsAnnotation.class)){//get the date in the sentence
                //get the date
                if(mention.get(CoreAnnotations.NamedEntityTagAnnotation.class).equals("DATE")){
                    //got a date
                    String date = mention.get(CoreAnnotations.TextAnnotation.class);
                    System.out.println("DATE: "+date);
                    result.setDate(date);
                    dateMentions.add(date);
                }
            }

            //subject will use dependency
            SemanticGraph graph = sentence.get(SemanticGraphCoreAnnotations.BasicDependenciesAnnotation.class);//break the sentence down into a graph
            System.out.println("Edge Count: " +graph.edgeCount());
            System.out.print("Subject: ");//set subject as location,person,company and/or subjects in the sentence.
            for(SemanticGraphEdge semanticGraphEdge: graph.edgeIterable()){//for each edge in the graph, find ones that are a relation to the subject of the sentence
                if(semanticGraphEdge.getRelation().getShortName().contains("subj")){//found a relation that contains some sort of subject in the sentence
                    //later split it into more ifs
                    System.out.print(semanticGraphEdge.toString()+"; ");
                    //System.out.println(semanticGraphEdge.toString());
                    result.addToSubject(semanticGraphEdge.getDependent().value());
                }
            }
            System.out.println("\n");

            //subject can LOCATION;PERSON;COMPANY and nsubj
            //now need to summarise for the event

            for(SemanticGraphEdge semanticGraphEdge: graph.edgeIterable()){//now for each edge, just print the relation
                System.out.println(String.format("%s(%s,%s)",semanticGraphEdge.getRelation().getShortName(),semanticGraphEdge.getGovernor()
                        +"-"+semanticGraphEdge.getGovernor().index(),semanticGraphEdge.getDependent()+"-"+semanticGraphEdge.getDependent().index()));
            }
            System.out.println("\n");
            result.addToEvent(summarized(graph));//now what is the event, summarize what happens in the sentence
            results.add(result);

            stanfordCoreNLP.prettyPrint(annotation,new PrintWriter(System.out));
            edu.stanford.nlp.trees.Tree tree;
            tree = sentence.get(TreeCoreAnnotations.TreeAnnotation.class);
            System.out.println("Printing a tree");
            tree.pennPrint();

            edu.stanford.nlp.trees.Tree root = tree.firstChild();
            System.out.println("Got root");
            Label rootLabel = root.label();
            System.out.println(rootLabel.value());
            System.out.println("\nPreOrder");
            ArrayList<Tree> nodes = new ArrayList<>();
            result.setEvent(event(tree));
/*
            //get the leftmost-lowest S
            for(Tree tree1: tree.postOrderNodeList()){
                System.out.println(tree1.value());
                if(tree1.value().equals("S")){
                    nodes.add(tree1);//nodes are added in post order so nodes on same depth, leftmost added in first
                }
            }
            edu.stanford.nlp.trees.Tree leftmostLowest = null;
            for(Tree tree1: nodes){
                if(leftmostLowest == null){
                    leftmostLowest = tree1;
                }else if(root.depth(leftmostLowest)  < root.depth(tree1)){//only set if its lower down, if same depth wont set
                    leftmostLowest = tree1;
                }
            }
            System.out.println("Lowest S is: "+leftmostLowest);
            System.out.println("S has "+root.children().length+ " children");
*/
/*

            //remove determiners
            String text1 = "";
            for(Tree tree1: leftmostLowest.preOrderNodeList()){
                //if(tree1.value().equals(""))
                if(!tree1.isLeaf()){
                    Tree childTree = tree1.getChild(0);
                    if(!childTree.isLeaf() && childTree.value().equals("DT") && (childTree.children()[0].value().equals("the") || childTree.children()[0].value().equals("a"))){
                        System.out.println("About to delete DT and its child");
                        tree1.removeChild(0);
                    }
                }
*/
/*                if(!tree1.isLeaf() && tree1.value().equals("DT") && (tree1.children()[0].value().equals("the") || tree1.children()[0].value().equals("a"))){
                    System.out.println("Deleting child that is DT");
                    tree1.removeChild(0);
                    tree1
                    //then remove yourself
                }*//*

                */
/*if(tree1.isLeaf() && !tree1.value().equals("the") && !tree1.value().equals("a")){
                    text1 += tree1.value()+" ";

                }*//*

            }
            System.out.println("About to print after attempted deletion:");
            leftmostLowest.pennPrint();
*/


/*
            System.out.println(text1);
            //make a new tree with the resulting text
            Annotation newAnnon = new Annotation(text1);
            stanfordCoreNLP.annotate(newAnnon);
            List<CoreMap> coreMap = newAnnon.get(CoreAnnotations.SentencesAnnotation.class);
            //need to make it into a coreMap before getting a tree, else null pointer
            System.out.println("Size of core map: "+coreMap.size());
            Tree tree1 = coreMap.get(0).get(TreeCoreAnnotations.TreeAnnotation.class);
            //to see size of a trees leafs(which is the number of words in the text, words like: long-running count as 1)
            System.out.println("Size of tree1 leafs: "+tree1.yield(new ArrayList<Label>()).size());
            System.out.println("Szie of text1: "+text1.length());//string.length gives you character length

*/

            //TODO:remove time expressions

            //then delete until text is below threshold
                //xp-over-xp
                    //where you have [XP[XP ...]...] delete the other children of the higher XP, both XP have to be same type (eg NP NP)
            //                       children of this one
            //delete iteratively, work your way up  until threshold is reached else continue below
/*
            xpOverXp(tree);
            System.out.println("Resulting tree:");
            tree.pennPrint();
*/

            //TODO:
            //delete any XP before the first NP (the subject) of the S chosen before
                //look at the children of S, if one is NP, then remember its location
                //then loop over list
                //if node != node with np (can remove if node is XP (including PP?) but parent is not S (where its parent is root) or ROOT if it is
                // then break for efficiency, as we reached subject)
                //and remove any NP and its children that came before this location

            //remove PPs from deepest rightmost node until length is below threshold (if not reached, then go back and do SBAR one)
            //remove SBARs from deepest rightmost node until length is below threshold
            //if threshold still not reached, then remove PPs from result of SBARs
            //then produce the final string with the data in the leaves, in the order in which they appear
            //and set that as the event in the result.
        }
        return results;
    }

    private String event(Tree root){
        //possibly change the leftmost lowest, as its going more for first with lowest depth, maybe get first lowest?
        //if cant find then should just use root of the tree
        ArrayList<Tree> nodes = new ArrayList<>();
        //get the leftmost-lowest S
        for(Tree tree1: root.postOrderNodeList()){
            System.out.println(tree1.value());
            if(tree1.value().equals("S")){
                nodes.add(tree1);//nodes are added in post order so nodes on same depth, leftmost added in first
            }
        }
        edu.stanford.nlp.trees.Tree leftmostLowest = null;
        for(Tree tree1: nodes){
            if(leftmostLowest == null){
                leftmostLowest = tree1;
            }else if(root.depth(leftmostLowest)  < root.depth(tree1)){//only set if its lower down, if same depth wont set
                leftmostLowest = tree1;
            }
        }
        //leftmostLowest = root;
        System.out.println("Lowest S is: "+leftmostLowest);

        //remove determiners
        for(Tree tree1: leftmostLowest.preOrderNodeList()){
            if(!tree1.isLeaf()){
                Tree childTree = tree1.getChild(0);
                if(!childTree.isLeaf() && childTree.value().equals("DT") && (childTree.children()[0].value().equals("the") || childTree.children()[0].value().equals("a"))){
                    System.out.println("About to delete DT and its child");
                    tree1.removeChild(0);
                }
            }
        }

        //TODO:remove time expressions could use named entity tag to determine them and remove them

        //then delete until text is below threshold
        //xp-over-xp
        //where you have [XP[XP ...]...] delete the other children of the higher XP, both XP have to be same type (eg NP NP)
        //                       children of this one
        //delete iteratively, work your way up  until threshold is reached else continue below
        xpOverXp(leftmostLowest);
        System.out.println("Resulting tree:");
        leftmostLowest.pennPrint();
        //delete any XP before the first NP (the subject) of the S chosen before
        //look at the children of S, if one is NP, then remember its location
        //then loop over list
        //if node != node with np (can remove if node is XP (including PP?) but parent is not S (where its parent is root) or ROOT if it is
        // then break for efficiency, as we reached subject)
        //and remove any NP and its children that came before this location
        xpBeforeNp(leftmostLowest);


        //TODO:
        //remove PPs from deepest rightmost node until length is below threshold (if not reached, then go back and do SBAR one)
        //remove SBARs from deepest rightmost node until length is below threshold
        //if threshold still not reached, then remove PPs from result of SBARs
        //then produce the final string with the data in the leaves, in the order in which they appear
        //and set that as the event in the result.
        Tree finalRes = shortenLast(leftmostLowest);

        String toReturn = produceString(finalRes);
        System.out.println("Event: "+toReturn);
        //possibly final cleanup, eg sentences that start or end with , or dots
        return toReturn;
    }

    /**
     * will try to remove rightmost-lowest PP until reach threshold, if still after that below threshold, then undo changes, and remove rightmost-lowest SBAR
     * until threshold is reached, if cant be reached then remove rightmost lowest PP until threshold is reached.
     * Returns a tree that has gone through the shortening of size smaller than threshold, or a tree where nothing can be removed
     * @param tree
     * @return
     */
    private Tree shortenLast(Tree tree){
        System.out.println("Shortening");
        int threshold = 10;
        if(tree.yield(new ArrayList<Label>()).size() > threshold) {
            //copy to use on the PPs
            Tree copy = tree.deepCopy();
            System.out.println("Entered RemovePP, before: ");
            tree.pennPrint();
            removePPs(copy);
            System.out.println("After: ");
            copy.pennPrint();
            if(copy.yield().size() > threshold){
                //werent able to reduce it enough, so use SBAR on tree and then do PP
                System.out.println("Have to remove further: do SBAR then PP");
                System.out.println("Entering removeSBAR, before:");
                tree.pennPrint();
                removeSBARs(tree);
                System.out.println("LEft removeSBAR, after: ");
                tree.pennPrint();
                System.out.println("Entering removePP,");
                removePPs(tree);
                System.out.println("Left removePP: ");
                tree.pennPrint();
            }else{
                return copy;//dont need to do anything else if we reached the threshold
            }
        }
        return tree;
    }

    private void removePPs(Tree tree){
        int threshold = 10;
        if(tree.yield(new ArrayList<Label>()).size() > threshold){

            //remove PP from the depeest right most node, which would be the last PP node found in preorder traversal
            Tree toDeleteChild = null; int indexToDelete = 0;
            for(Tree node: tree.preOrderNodeList()){
                if(!node.isLeaf()){
                    Tree[] children = node.children();
                    for(int i= 0; i<children.length; i++){
                        if(children[i].value().equals("PP")){//last PP found so keep looping over the entire list, last one set to toDeleteChild will be rightmost lowest PP
                            //found child PP
                            toDeleteChild = node;
                            indexToDelete = i;
                        }
                    }
                }
            }
            if(toDeleteChild != null){
                Tree deleted = toDeleteChild.removeChild(indexToDelete);
                System.out.println("Deleted child from: "+toDeleteChild+" at index: "+indexToDelete+"   "+deleted);
                removePPs(tree);
            }else{
                return;
            }
        }
    }

    private void removeSBARs(Tree tree){
        int threshold = 10;
        if(tree.yield(new ArrayList<Label>()).size() > threshold){
            //remove PP from the depeest right most node, which would be the last PP node found in preorder traversal
            Tree toDeleteChild = null; int indexToDelete = 0;
            for(Tree node: tree.preOrderNodeList()){
                if(!node.isLeaf()){
                    Tree[] children = node.children();
                    for(int i= 0; i<children.length; i++){
                        if(children[i].value().equals("SBAR")){//last PP found so keep looping over the entire list, last one set to toDeleteChild will be rightmost lowest PP
                            //found child PP
                            toDeleteChild = node;
                            indexToDelete = i;
                        }
                    }
                }
            }
            if(toDeleteChild != null){
                Tree deleted = toDeleteChild.removeChild(indexToDelete);
                System.out.println("Deleted child from: "+toDeleteChild+" at index: "+indexToDelete+"   "+deleted);
                removeSBARs(tree);
            }else{
                return;
            }
        }
    }

    private String produceString(Tree tree){
        String toReturn = "";
        for(Tree node: tree.preOrderNodeList()){
            if(node.isLeaf()) {
                toReturn += node.value()+ " ";
            }
        }
        return toReturn;
    }

    /**
     * Delete any XP (PP,NP,VP) before the subject of the sentence which is the NP child of S, if tree size is > than threshold
     * Find NP subject of S
     * then do pre order, if we reach NP of S then stop
     * if before that we reach an XP that isnt the the NP of S, then store its parent and its index as a child
     * and remove it from the tree
     * @param tree
     */
    private void xpBeforeNp(Tree tree){
        int threshold = 10;
        System.out.println("Tree before going through: ");
        tree.pennPrint();
        System.out.println("Are we shortening? size: "+tree.yield(new ArrayList<Label>()).size());
        if(tree.yield(new ArrayList<Label>()).size() > threshold){
            System.out.println("Going to try to reduce");
            //Tree root = tree.firstChild();//as tree is just ROOT, that holds an S
            if(tree.value().equals("S")){
                System.out.println("Root is S");
                Tree[] childrenOfRoot = tree.children();
                Tree pointToFirstNp = null;
                for(int i=0; i<childrenOfRoot.length; i++){
                    Tree child = childrenOfRoot[i];
                    if(child.value().equals("NP")){//looking for the subject of the sentence
                        pointToFirstNp = child;
                        System.out.println("NP found: "+pointToFirstNp);
                        break;
                    }
                }
                if(pointToFirstNp != null){//found a NP now need to check before it and delete preamble which is any NP before it
                    Tree toDeleteNode = null;
                    int childIndex = 0;
                    for(Tree node: tree.preOrderNodeList()){
                        if(node == pointToFirstNp || toDeleteNode != null){//as its pre order any node thats np before this will be looped through first
                            break;
                        }else if(!node.isLeaf() ){//we are only deleting child nodes, so node has to be a parent
                            Tree[] children = node.children();
                            for(int i=0; i<children.length; i++){
                                Tree child = children[i];
                                if(child == pointToFirstNp){//if we going through the root then one of the children will be NP so if we reached it skip checking others and go down levels
                                    break;
                                }
                                if(child != pointToFirstNp && (child.value().equals("VP") || child.value().equals("NP") || child.value().equals("PP"))){//added the removal of PP
                                    toDeleteNode = node; childIndex = i;
                                    break;
                                }
                            }
                        }
                    }
                    if(toDeleteNode != null){
                        System.out.println("Removing from: "+toDeleteNode+ " child at index: "+childIndex);
                        toDeleteNode.removeChild(childIndex);
                    }
                }
            }
        }
        System.out.println("Tree after going through: ");
        tree.pennPrint();
    }

   /**
   		go through preorder finding the last one with xp xp (so check for np-np,vp-vp,and s-s)
		then remove all children except first of that one.
		then check if size of leaf nodes is less than threshold, if not repeat
    **/
    private void xpOverXp(Tree root){
        int threshold = 10;
        if(root.yield(new ArrayList<Label>()).size() > threshold) {//if we are past the threshold then we should try to reduce
            System.out.println("In xp-over-xp");
            //root.pennPrint();
            Tree toRemoveChildren = null;
            for (Tree tree1 : root.preOrderNodeList()) {//need to check that we have something to delete, else would loop forever on the same nodes
                if (!tree1.isLeaf() && tree1.children().length > 1 && (
                        (tree1.value().equals("NP") && tree1.children()[0].value().equals("NP"))
                                || (tree1.value().equals("VP") && tree1.children()[0].value().equals("VP"))
                                || (tree1.value().equals("S") && tree1.children()[0].value().equals("S"))
                )) {//found a node whos first child is also of the same type (NP-over-NP or VP-over-VP or S-over-S)
                    System.out.println("Found a value to remove");
                    toRemoveChildren = tree1;
                }
            }
            if (toRemoveChildren != null) {//got a node to delete its children from
                while (toRemoveChildren.children().length > 1) {
                    Tree removed = toRemoveChildren.removeChild(1);
                    System.out.println("Removed: "+removed);
                }
                //now should only have one child
                //should check whether we are below sentence threshold
               xpOverXp(root);
                //if not then do it again, else leave
            }else{
                return;
            }
        }
    }

    private ArrayList<String> summarized(SemanticGraph graph){
        ArrayList<String> strings = new ArrayList<>();
        for(SemanticGraphEdge semanticGraphEdge: graph.edgeIterable()){//for each edge
            if(semanticGraphEdge.getRelation().getShortName().contains("subj")){//look at the ones that are subject relation (could group with setting subject tag)
                //find all relations that contain dep
                System.out.println("For relation: "+String.format("%s(%s,%s)",semanticGraphEdge.getRelation().getShortName(),
                        semanticGraphEdge.getGovernor().value(),semanticGraphEdge.getDependent().value()));
                ArrayList<IndexedWord> indexedWords = new ArrayList<>();
                IndexedWord dep = semanticGraphEdge.getDependent();//for the dependent value in the relation,
                searchDep(dep,graph,indexedWords);// find other relations that contain it

                IndexedWord gov = semanticGraphEdge.getGovernor();//for the governor value in the relation,
                searchGov(gov,graph,indexedWords);// find other relations that contain it
                indexedWords.add(dep);//then add them to the list of words that are part of the summary
                indexedWords.add(gov);
                for(IndexedWord indexedWord: indexedWords){//print to see what we have
                    System.out.println(indexedWord.value());
                }

                //now for new ones, passing in the list to avoid duplicates, need a copy of the original array as we will add to the
                //original while looping which throws an exception
                ArrayList<IndexedWord> newIndexedWords = new ArrayList<>(indexedWords);
                for(IndexedWord indexedWord: newIndexedWords){//for the new ones added, lets find others linked to them (no duplicates allowed in the list
                                                                //thats why the list is passed in as a check is done
                    searchDep(indexedWord,graph,indexedWords);
                }

                System.out.println("Size of indexedWords: "+indexedWords.size());

                //now order them by their index in the original sentence
                Collections.sort(indexedWords, new Comparator<IndexedWord>() {
                    @Override
                    public int compare(IndexedWord o1, IndexedWord o2) {
                        return new Integer(o1.index()).compareTo(new Integer(o2.index()));
                    }
                });
                System.out.println("\n\nPrinting indexed words");
                String toAdd = "";//now form a sentence with the words that we obtained
                for(IndexedWord indexedWord: indexedWords){
                    toAdd += indexedWord.value()+" ";
                }//and add them to the list of summarized sentences
                strings.add(toAdd);
            }
        }

        return strings;
    }

    /**
     * Could be combined with the below one.
     * @param indexedWord
     * @param semanticGraph
     * @param indexedWords
     */
    private void searchDep(IndexedWord indexedWord, SemanticGraph semanticGraph, ArrayList<IndexedWord> indexedWords){
        //ArrayList<IndexedWord> indexedWords = new ArrayList<>();
        System.out.println("For: "+indexedWord.value());
        for(SemanticGraphEdge semanticGraphEdge: semanticGraph.edgeIterable()){//go through the edges
            if(!semanticGraphEdge.getRelation().getShortName().contains("subj")){//if we find relations that arent subject related
                System.out.println("Looking at: "+String.format("%s(%s,%s)",semanticGraphEdge.getRelation().getShortName()
                                        , semanticGraphEdge.getGovernor().value(),semanticGraphEdge.getDependent().value()));
                if(semanticGraphEdge.getDependent().equals(indexedWord) && !indexedWords.contains(semanticGraphEdge.getGovernor())){
                    System.out.print("Adding Governer ");//add the other value in the relation to the list, if its not already in the list
                    indexedWords.add(semanticGraphEdge.getGovernor());
                    System.out.print("Added: "+semanticGraphEdge.getGovernor().value()+"\n");
                }else if(semanticGraphEdge.getGovernor().equals(indexedWord) && !indexedWords.contains(semanticGraphEdge.getDependent())){
                    System.out.print("Adding Governer ");
                    indexedWords.add(semanticGraphEdge.getDependent());
                    System.out.print("Added: "+semanticGraphEdge.getDependent().value()+"\n");
                }

            }
        }
    }




    private void searchGov(IndexedWord indexedWord, SemanticGraph semanticGraph, ArrayList<IndexedWord> indexedWords){
        System.out.println("For: "+indexedWord.value());
        for(SemanticGraphEdge semanticGraphEdge: semanticGraph.edgeIterable()){
            if(!semanticGraphEdge.getRelation().getShortName().contains("subj")){
                System.out.println("Looking at: "+String.format("%s(%s,%s)",semanticGraphEdge.getRelation().getShortName()
                        , semanticGraphEdge.getGovernor().value(),semanticGraphEdge.getDependent().value()));
                if(semanticGraphEdge.getDependent().equals(indexedWord) && !indexedWords.contains(semanticGraphEdge.getGovernor())){
                    System.out.print("Adding Governer ");
                    indexedWords.add(semanticGraphEdge.getGovernor());
                    System.out.print("Added: "+semanticGraphEdge.getGovernor().value()+"\n");
                }else if(semanticGraphEdge.getGovernor().equals(indexedWord) && !indexedWords.contains(semanticGraphEdge.getDependent())){
                    System.out.print("Adding Dependent ");
                    indexedWords.add(semanticGraphEdge.getDependent());
                    System.out.print("Added: "+semanticGraphEdge.getDependent().value()+"\n");
                }
            }
        }
    }

}
