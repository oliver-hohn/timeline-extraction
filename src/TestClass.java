import java.io.File;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;

/**
 * Created by Oliver on 29/10/2016.
 */
public class TestClass {

    public static void main(String[] args) {
        //String text = "";
/*
        String text = "I can almost always tell when movies use fake dinosaurs. On the 12th of December I played football. " +
                "Yesterday Marry disappeared while running on North St, it was a cold night. ";
        text += "John, who was the CEO of a company, played golf. On the 12th of December I played Basketball with Oliver and Tom. Yesterday," +
                " I studied for 40 hours, lmao. ";

        text += "A fire killed a firefighter who was fatally injured as he searched the house. Illegal fireworks injured hundreds of people and started six fires. ";

        text += "On Friday the Washington Post came out with the latest from its long-running investigation into Trump's charitable donations.\n" +
                "\n" +
                "In its latest story, the paper called 420-plus charities with some connection to Trump but found only one personal gift from him between 2008 and the spring of this year. \n" +
                "\n" +
                "But the Post did find nearly $8m that Trump has donated from his own pocket since the early 1980s.\n" +
                "\n" +
                "One of the bizarre episodes the paper recounts is that in 1996, Trump showed up without an invitation to a charity for the Association to Benefit Children where he took a seat on the stage that had been reserved for a major donor, despite not being a donor himself.\n" +
                "\n" +
                "In response the piece, the Trump campaign told the Post that he \"has personally donated tens of millions of dollars... to charitable causes\".";
        text += "Last year I did well at uni. Last Month I went to school. Last week I played basketball. Every day in January, I play football.";

        System.out.println("Processing hard-coded text:");
        Engine engine = new Engine();
        String date = "2016-11-30";
        ArrayList<Result> results1 = engine.getResults(text,date);
        for(Result result: results1){
            System.out.println(result);
        }

        System.out.println("\n\nSorted List:");

        Collections.sort(results1);
        for(Result result: results1){
            System.out.println(result);
        }
*/

        System.out.println("\n\nProcessing through files");
        ProcessFiles processFiles = new ProcessFiles();
        File file1 = new File("D:"+File.separator+"FYP"+File.separator+"text1.txt");
        File file2 = new File("D:"+File.separator+"FYP"+File.separator+"text2.txt");
        File file3 = new File("D:"+File.separator+"FYP"+File.separator+"text3.txt");
        File file4 = new File("D:"+File.separator+"FYP"+File.separator+"text4.txt");
        ArrayList<File> files = new ArrayList<>();
        files.add(file1); files.add(file2);
        files.add(file3);
        files.add(file4);
        processFiles.processFiles(files, new CallbackResults() {
            @Override
            public void gotResults(ArrayList<Result> results) {
                System.out.println("Finished running, got: "+results.size()+" results");
                //would have to sort it
                Collections.sort(results);
                for(Result result: results){
                    System.out.println(result);
                }
            }
        });

    }

}
