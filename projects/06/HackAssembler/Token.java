/** A token object represents a piece of Assembly code and is just a data object to store
 *  a piece of text and some data about where the text came from (filename, line number).
 *
 *  The text is the only field that can be updated after the Token has been created.
 */
public class Token {
    private String text;
    private String fileName;
    private int lineNo;

    public Token(String t, String f, int l) {
        text = t;
        fileName = f;
        lineNo = l;
    }

    public boolean isNull() {
        return text.equals("null");
    }

    public String text() {
        return text;
    }

    public String fileName() {
        return fileName;
    }

    public void setText(String s) {
        text = s;
    }

    public int lineNumber() {
        return lineNo;
    }

    public String toString() {
        return text;
    }

    public String details() {
        return text + " (" + fileName + ": Line " + lineNo + ")";
    }
}
