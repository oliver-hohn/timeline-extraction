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
 * Class used to represent a list of Results in a PDF.
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
    private PDDocument pdDocument;
    private PDPage currentPage;
    private PDPageContentStream contentStream;

    /**
     * Constructor called to prepare for the creation of the PDF.
     */
    public ToPDF() {
        pdDocument = new PDDocument();
        try {
            reset();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Will set all the global variables, and close any open streams.
     *
     * @throws IOException when closing a stream that could be open or closed.
     */
    private void reset() throws IOException {
        currentPage = new PDPage(PDRectangle.A4);
        pdDocument.addPage(currentPage);
        if (contentStream != null) {
            contentStream.close();
            contentStream = null;
        }
        contentStream = new PDPageContentStream(pdDocument, currentPage);
        contentStream.setStrokingColor(Color.black);
        contentStream.setLineWidth(strokeWidthOfLine);

        widthOfPage = currentPage.getMediaBox().getWidth();
        heightOfPage = currentPage.getMediaBox().getHeight();

        currentX = 0;
        currentY = (int) heightOfPage;

        contentStream.moveTo(widthOfPage / 2, heightOfPage);
        contentStream.lineTo(widthOfPage / 2, 0);
        contentStream.stroke();
    }


    /**
     * For the given Results, produce a PDF File that has all the given Results displayed in a timeline (like on the
     * Application but without the buttons).
     *
     * @param results  the list of given Results.
     * @param filename the name for the File we are storing
     * @throws IOException due to working with streams.(I.e. trying to read or remove a file that is already open).
     */
    public void saveToPDF(List<Result> results, String filename) throws IOException {
        int counterOfEvents = 0;
        for (int i = 0; i < results.size(); i++) {
            if (counterOfEvents >= 5) {//start a new page
                reset();
                counterOfEvents = 0;
            }
            if (i % 2 == 0) {//even
                drawEvenEvent(results.get(i), contentStream);
            } else {//odd
                drawOddEvent(results.get(i), contentStream);
            }
            counterOfEvents++;
        }
        if (contentStream != null) {
            contentStream.close();
        }
        pdDocument.save(new File(filename + ".pdf"));
        pdDocument.close();
    }

    /**
     * Called to draw the event in the timeline for a Result that is at a Odd index in the list.
     *
     * @param result        the given Result object for which to draw this event.
     * @param contentStream the stream to which we are drawing.
     * @throws IOException due to working with streams.
     */
    private void drawOddEvent(Result result, PDPageContentStream contentStream) throws IOException {
        System.out.println("Here at Odd");
        //initially y is the top right where this needs to be shown, x starts from the middle
        currentY -= padding; //add some padding to y
        currentX = (int) widthOfPage / 2;
        contentStream.moveTo(currentX, currentY);
        System.out.println("X: " + currentX + " Y: " + currentY);
        //write the text for the Event
        int lengthOfHorLine = (int) ((widthOfPage / 2) - (padding + widthOfRectangle));
        currentX += lengthOfHorLine;
        writeText(result, contentStream, currentX);
        //draw the rectangle to surround the text
        drawRectangle(contentStream, currentX, currentY - heightOfRectangle);
        //draw the horizontal line connecting event and timeline
        currentY -= heightOfRectangle / 2;
        contentStream.moveTo(currentX, currentY);
        contentStream.lineTo(widthOfPage / 2, currentY);
        contentStream.stroke();

        currentY -= (heightOfRectangle / 2) + padding;
    }

    /**
     * Called to draw the event in the timeline for a Result that is at a Even index in the list.
     *
     * @param result        the given Result object for which to draw this event.
     * @param contentStream the stream to which we are drawing.
     * @throws IOException due to working with streams.
     */
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
        drawRectangle(contentStream, currentX, currentY - heightOfRectangle);
        //draw the horizontal line connecting event and timeline
        currentX += widthOfRectangle;
        currentY -= heightOfRectangle / 2;
        contentStream.moveTo(currentX, currentY);
        contentStream.lineTo(widthOfPage / 2, currentY);
        contentStream.stroke();

        currentY -= (heightOfRectangle / 2) + padding;//could set a string padding here

        //now connected the event (text surrounded by rectangle) to the timeline
    }

    /**
     * For the given Result and stream, write at the current x and y position (in new lines) the data held by the
     * Result object.
     *
     * @param result        the given Result.
     * @param contentStream the stream we are drawing to.
     * @param xOffset       an x-value offset from which the text is written from (starting at currentX)
     * @throws IOException due to using streams.
     */
    private void writeText(Result result, PDPageContentStream contentStream, int xOffset) throws IOException {
        contentStream.beginText();
        contentStream.newLineAtOffset(2 * padding + xOffset, currentY - (fontSize + padding));//pad it horizontally and give vertical space for text
        contentStream.setFont(PDType1Font.TIMES_ROMAN, fontSize);
        contentStream.showText("Event #");
        contentStream.newLineAtOffset(0, -(fontSize + padding));
        contentStream.showText("Date: " + result.getTimelineDate());
        contentStream.newLineAtOffset(0, -(fontSize + padding));
        contentStream.showText("Subjects: " + result.getSubjectsAsString());
        contentStream.newLineAtOffset(0, -(fontSize + padding));
        contentStream.showText("Event: " + result.getEvent());
        contentStream.newLineAtOffset(0, -(fontSize + padding));
        contentStream.showText("From: " + result.getFileData().getFileName());
        contentStream.endText();
    }

    /**
     * For the given bottom left x and y co-ordinates, draw a rectangle (its width and height are given by the static final
     * member values).
     *
     * @param contentStream where the Rectangle is being drawn to.
     * @param bottomLeftX   the x-coordinate of the bottom left of the rectangle.
     * @param bottomLeftY   the y-coordinate of the bottom left of the rectangle
     * @throws IOException
     */
    private void drawRectangle(PDPageContentStream contentStream, int bottomLeftX, int bottomLeftY) throws IOException {
        contentStream.addRect(bottomLeftX, bottomLeftY, widthOfRectangle, heightOfRectangle);
        contentStream.stroke();
    }


}
