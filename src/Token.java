/**
 * Created by Oliver on 29/10/2016.
 */
public class Token {
    private String text;
    private String pos;

    public Token(String text, String pos){
        this.text = text;
        this.pos = pos;
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }

    public String getPos() {
        return pos;
    }

    public void setPos(String pos) {
        this.pos = pos;
    }

    public String toString(){
        return String.format("Text: %s, Pos: %s",text,pos);
    }
}
