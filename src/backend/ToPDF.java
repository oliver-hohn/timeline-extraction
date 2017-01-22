package backend;

import backend.process.Result;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.common.PDRectangle;
import org.apache.pdfbox.pdmodel.font.PDType1Font;

import java.awt.*;
import java.io.File;
import java.io.IOException;
import java.util.List;

/**
 * Created by Oliver on 22/01/2017.
 */
public class ToPDF {
    private static int widthOfRectangle = 275;
    private static int heightOfRectangle = 150;
    private static int strokeWidthOfLine = 2;
    private static int padding = 7;
    private static int fontSize = 12;
    private int currentX = 0;
    private int currentY = 0;
    private float widthOfPage;
    private float heightOfPage;


    public void saveToPDF(List<Result> results, String filename) throws IOException {
        PDDocument document = new PDDocument();
        PDPage currentPage = new PDPage(PDRectangle.A4);
        document.addPage(currentPage);
        PDPageContentStream content = new PDPageContentStream(document, currentPage);
        content.setStrokingColor(Color.black);
        content.setLineWidth(strokeWidthOfLine);


        widthOfPage = currentPage.getMediaBox().getWidth();
        heightOfPage = currentPage.getMediaBox().getHeight();

        System.out.println("Max Width: "+widthOfPage+" Max Height: "+heightOfPage);

        currentX = 0;
        currentY = (int) heightOfPage;

        content.moveTo(widthOfPage/2, currentY);
        content.lineTo(widthOfPage/2, 0);
        currentX= 0;
        currentY = (int) heightOfPage;

        content.stroke();

        int counterOfEvents = 0;
        for(int i=0; i<results.size(); i++){
            if(counterOfEvents >= 5){//start a new page
                content.close();//TODO: refactor into one method & wrap text
                currentPage = new PDPage(PDRectangle.A4);
                document.addPage(currentPage);
                content = new PDPageContentStream(document, currentPage);
                content.setStrokingColor(Color.black);
                content.setLineWidth(strokeWidthOfLine);
                widthOfPage = currentPage.getMediaBox().getWidth();
                heightOfPage = currentPage.getMediaBox().getHeight();

                System.out.println("Max Width: "+widthOfPage+" Max Height: "+heightOfPage);

                currentX = 0;
                currentY = (int) heightOfPage;

                content.moveTo(widthOfPage/2, currentY);
                content.lineTo(widthOfPage/2, 0);
                currentX= 0;
                currentY = (int) heightOfPage;

                content.stroke();
                counterOfEvents = 0;
            }
            if(i%2==0){//even
                drawEvenEvent(results.get(i), content);
            }else{//odd
                drawOddEvent(results.get(i), content);
            }
            counterOfEvents++;
        }
        content.close();
        document.save(new File(filename+".pdf"));
        document.close();
    }

    private void drawOddEvent(Result result, PDPageContentStream contentStream) throws IOException{
        System.out.println("Here at Odd");
        //initially y is the top right where this needs to be shown, x starts from the middle
        currentY -= padding; //add some padding to y
        currentX = (int) widthOfPage/2;
        contentStream.moveTo(currentX, currentY);
        System.out.println("X: "+currentX+" Y: "+currentY);
        //write the text for the Event
        int lengthOfHorLine = (int) ((widthOfPage/2) -(padding+widthOfRectangle));
        currentX += lengthOfHorLine;
        writeText(result, contentStream, currentX);
        //draw the rectangle to surround the text
        drawRectangle(contentStream, currentX, currentY-heightOfRectangle);
        //draw the horizontal line connecting event and timeline
        currentY -= heightOfRectangle/2;
        contentStream.moveTo(currentX, currentY);
        contentStream.lineTo(widthOfPage/2, currentY);
        contentStream.stroke();

        currentY -= (heightOfRectangle/2)+padding;
    }


    private void drawEvenEvent(Result result, PDPageContentStream contentStream) throws IOException {
        System.out.println("Here at Even");
        //initially x and y are top right in the page (0, pageHeight)
        //give some spacing between events
        currentY -= padding;//add some vertical small padding
        currentX = padding;
        contentStream.moveTo(currentX, currentY);
        //write the text for the Event
        writeText(result, contentStream, 0);
        //draw the rectangle to surround the text
        drawRectangle(contentStream, currentX, currentY-heightOfRectangle);
        //draw the horizontal line connecting event and timeline
        currentX += widthOfRectangle;
        currentY -= heightOfRectangle/2;
        contentStream.moveTo(currentX, currentY);
        contentStream.lineTo(widthOfPage/2, currentY);
        contentStream.stroke();

        currentY -= (heightOfRectangle/2)+padding;//could set a string padding here

        //now connected the event (text surrounded by rectangle) to the timeline
    }

    private void writeText(Result result, PDPageContentStream contentStream, int xOffset) throws IOException {
        contentStream.beginText();
        contentStream.newLineAtOffset(2*padding+xOffset, currentY-(fontSize+padding));//pad it horizontally and give vertical space for text
        contentStream.setFont(PDType1Font.TIMES_ROMAN, fontSize);
        contentStream.showText("Event #");
        contentStream.newLineAtOffset(0, -(fontSize+padding));
        contentStream.showText("Date: "+result.getTimelineDate());
        contentStream.newLineAtOffset(0, -(fontSize+padding));
        contentStream.showText("Subjects: "+result.getSubjectsAsString());
        contentStream.newLineAtOffset(0, -(fontSize+padding));
        contentStream.showText("Event: "+result.getEvent());
        contentStream.newLineAtOffset(0, -(fontSize+padding));
        contentStream.showText("From: "+result.getFileData().getFileName());
        contentStream.endText();
    }

    private void drawRectangle(PDPageContentStream contentStream, int bottomLeftX, int bottomLeftY) throws IOException {
        contentStream.addRect(bottomLeftX, bottomLeftY, widthOfRectangle, heightOfRectangle);
        contentStream.stroke();
    }



}
