package backend;

import backend.process.Result;
import backend.process.TimelineDate;
import backend.ranges.ProduceRanges;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by Oliver on 29/10/2016.
 */
public class TestClass {

    public static void main(String[] args) throws ParseException {
        List<Result> resultList = new ArrayList<>();
        Result result;
        TimelineDate timelineDate;
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("dd-MM-yyyy G");


        result = new Result();
        timelineDate = new TimelineDate();
        timelineDate.setDate1(simpleDateFormat.parse("01-01-0001 AD"));
        timelineDate.setDate2(simpleDateFormat.parse("31-12-9999 AD"));
        timelineDate.getRange();
        result.setTimelineDate(timelineDate);
        resultList.add(result);
        result = new Result();
        timelineDate = new TimelineDate();
        timelineDate.setDate1(simpleDateFormat.parse("12-12-2016 AD"));
        timelineDate.setDate2(simpleDateFormat.parse("18-12-2016 AD"));
        result.setTimelineDate(timelineDate);
        timelineDate.getRange();
        resultList.add(result);
        result = new Result();
        timelineDate = new TimelineDate();
        timelineDate.setDate1(simpleDateFormat.parse("15-12-2016 AD"));
        timelineDate.setDate2(simpleDateFormat.parse("20-12-2016 AD"));
        result.setTimelineDate(timelineDate);
        timelineDate.getRange();
        resultList.add(result);
        result = new Result();
        timelineDate = new TimelineDate();
        timelineDate.setDate1(simpleDateFormat.parse("15-12-2016 AD"));
        timelineDate.setDate2(simpleDateFormat.parse("20-12-2016 AD"));
        result.setTimelineDate(timelineDate);
        timelineDate.getRange();
        resultList.add(result);
        result = new Result();
        timelineDate = new TimelineDate();
        timelineDate.setDate1(simpleDateFormat.parse("01-01-0499 BC"));
        timelineDate.setDate2(simpleDateFormat.parse("31-12-0400 BC"));
        result.setTimelineDate(timelineDate);
        timelineDate.getRange();
        resultList.add(result);

        ProduceRanges produceRanges = new ProduceRanges();
        produceRanges.produceRanges(resultList);
/*
        //to see the outputted pdf files format
        ArrayList<Result> results = new ArrayList<>();
        Result result = new Result();
        result.setEvent("Hey there buddy");
        result.addDate_1("2014-12-31", "1996-07-29");
        result.addSubject("Buddy");
        result.setFileData(new FileData("filename1.txt","Here"));
        results.add(result);
        result= new Result();
        result.setEvent("Yo yo yo we out here");
        result.addDate_1("2017-01-22","2017-01-22");
        result.addSubject("Here");
        result.setFileData(new FileData("filename2.txt","Not Here"));
        results.add(result);
        result= new Result();
        result.setEvent("Hey friends!");
        result.addDate_1("2017-01-25","2017-01-22");
        result.addSubject("Friends");
        result.setFileData(new FileData("filename3.txt","Not Here"));
        results.add(result);
        result= new Result();
        result.setEvent("Supreme x Louis Vuitton coming out tomorrow");
        result.addDate_1("2017-01-28","2017-01-22");
        result.addSubject("Supreme");
        result.addSubject("Louis Vuitton");
        result.setFileData(new FileData("filename4.txt","Not Here"));
        results.add(result);
        result= new Result();
        result.setEvent("Supreme x Air Jordan came out last year");
        result.addDate_1("2017-01-31","2017-01-22");
        result.addSubject("Supreme");
        result.addSubject("Air Jordan");
        result.setFileData(new FileData("filename5.txt","Not Here"));
        results.add(result);
        result = new Result();
        result.setEvent("Here is another Result");
        result.addDate_1("2014-12-31", "1996-07-29");
        result.addSubject("Result");
        result.setFileData(new FileData("filename1.txt","Here"));
        results.add(result);
        result= new Result();
        result.setEvent("Yo yo yo we out here (repeated)");
        result.addDate_1("2017-01-22","2017-01-22");
        result.addSubject("Here");
        result.setFileData(new FileData("filename2.txt","Not Here"));
        results.add(result);
        result= new Result();
        result.setEvent("We here and we aint going no where");
        result.addDate_1("2017-01-25","2017-01-22");
        result.addSubject("here");
        result.setFileData(new FileData("filename3.txt","Not Here"));
        results.add(result);
        result= new Result();
        result.setEvent("Supreme x Louis Vuitton");
        result.addDate_1("2017-01-28","2017-01-22");
        result.addSubject("Supreme");
        result.addSubject("Louis Vuitton");
        result.setFileData(new FileData("filename4.txt","Not Here"));
        results.add(result);
        result= new Result();
        result.setEvent("Supreme x Air Jordan");
        result.addDate_1("2017-01-31","2017-01-22");
        result.addSubject("Supreme");
        result.addSubject("Air Jordan");
        result.setFileData(new FileData("filename5.txt","Not Here"));
        results.add(result);
        result= new Result();
        result.setEvent("Supreme x Air Jordan, this is repeated");
        result.addDate_1("2017-01-31","2017-01-22");
        result.addSubject("Supreme");
        result.addSubject("Air Jordan");
        result.setFileData(new FileData("filename5.txt","Not Here"));
        results.add(result);
        try {
            new ToPDF().saveTo(results, new File("filename.pdf"));
        } catch (IOException e) {
            e.printStackTrace();
        }
*/

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
        backend.process.Engine engine = new backend.process.Engine();
        String date = "2016-11-30";
        ArrayList<backend.process.Result> results1 = engine.getResults(text,date);
        for(backend.process.Result result: results1){
            System.out.println(result);
        }

        System.out.println("\n\nSorted List:");

        Collections.sort(results1);
        for(backend.process.Result result: results1){
            System.out.println(result);
        }

*/
/*
        System.out.println("\n\nProcessing through files");
        ProcessFiles processFiles = new ProcessFiles();
        File file1 = new File("D:"+File.separator+"FYP"+File.separator+"text1.txt");
        File file2 = new File("D:"+File.separator+"FYP"+File.separator+"text2.txt");
        File file3 = new File("D:"+File.separator+"FYP"+File.separator+"text3.txt");
        File file4 = new File("D:"+File.separator+"FYP"+File.separator+"text4.txt");
        File file5 = new File("D:"+File.separator+"FYP"+File.separator+"text5.pdf");
        File file6 = new File("D:"+File.separator+"FYP"+File.separator+"text6.docx");
        ArrayList<File> files = new ArrayList<>();
        ArrayList<FileData> fileDatas = new ArrayList<>();
        for(File file: files){
            fileDatas.add(new FileData(file));
        }
        files.add(file1); files.add(file2);
        //files.add(file3);
        files.add(file4);
        files.add(file5);
        //files.add(file6);
        ArrayList<Result> results = processFiles.processFiles(files, fileDatas).first();
        System.out.println("Finished running, got: "+results.size()+" results");
        //would have to sort it
        Collections.sort(results);
        for(Result result: results){
            System.out.println(result);
        }
*/

/*        TimelineDate timelineDate = new TimelineDate();
        timelineDate.parse("XXXX-01", "2016-12-31");*/
/*        processFiles.processFiles(files, new backend.process.CallbackResults() {
            @Override
            public void gotResults(ArrayList<backend.process.Result> results) {
                System.out.println("Should never reach this, as it has not finished processing files");
            }//Should never run, as the we are Processing the other files (only if they are all done, can this run)
        });*/
/*        String text = "Now I am going to play playstation. In the 5th Century B.C. they played football. Next weekend I'm playing basketball. Next week we play football. Last week on Friday. Mary left on Thursday and John arrived the day after.";
        Engine engine = new Engine();
        ArrayList<Result> results = engine.getResults(text, "2016-12-30");
        Collections.sort(results);
        for(Result result: results){
            System.out.println(result);
        }*/
    }

}
