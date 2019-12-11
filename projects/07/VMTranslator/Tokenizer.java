import java.io.BufferedReader;
import java.io.FileReader;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;

public class Tokenizer {
    private BufferedReader reader;
    /** The name of the file to read tokens from */
    private String fileName;
    /** The number of the line in the file that is currently being processed. */
    private int lineNo;
    /** The index of the next char in the buffer. */
    private int currChar;
    /** An internal buffer to store one line of the input at a time. */
    private String buffer;
    /** endOfFile will be true if the last file read was not successful. */
    private boolean endOfFile;
    /** A string containing the characters that serve as tokens. */
    private Token pushBack;
    private String tokens;

    /** Build a tokenizer and fill the internal buffer with the first
     *  line from the file named fileName */
    public Tokenizer(String fileName, String tokens) {
        this.fileName = fileName;
        this.tokens = tokens;
        try {
            FileReader fr = new FileReader(fileName);
            reader = new BufferedReader(fr);
            currChar = 0;
            endOfFile = false;
            lineNo = 0;
            pushBack = null;
        } catch (FileNotFoundException ex) {
            System.out.println("Unable to open file '" + 
                    fileName + "'");
            endOfFile = true;
            System.exit(-1);
        }
        fillBuffer();
    }

    private int lineNumber() {
        return lineNo;
    }

    /** Fill the internal buffer with one line from the input file,
     *  skipping any leading whitespace, if the read operation fails
     *  or there are no remaining lines to be read, the buffer will
     *  be set to null and the endOfFile flag will be set to true. */
    private void fillBuffer() {
        try {
            buffer = reader.readLine();
            currChar = 0;
            lineNo += 1;
        } catch (IOException ex) {
            System.out.println("Error reading file '" +
                    fileName + "'");
            buffer = null;
            endOfFile = true;
            System.exit(-2);
        }
        if (buffer == null) {
            endOfFile = true;
        } else {
            skipWhitespace();
            stripComments();
            if (endOfLine()) {
                fillBuffer();
            }
        }
    }

    public boolean moreTokens() {
        return ! endOfLine();
    }

    /** Return true if we have processed every character in the internal
     *  buffer. */
    private boolean endOfLine() {
        return currChar == buffer.length();
    }

    private boolean isToken(char c) {
        return tokens.indexOf(c) >= 0;
    }

    /** Move the currChar index to the first non-whitespace character. */
    private void skipWhitespace() {
        while (buffer != null && !endOfLine()) {
            if (Character.isWhitespace(peekNextChar())) {
                currChar += 1;
            } else {
                break;
            }
        }
    }

    /** If the next two characters indicate the start of a comment,
     *  discard the rest of the current line and refill the buffer. */
    private void stripComments() {
        if (buffer != null && !endOfLine()) {
            if (peekNextChar() == '/') {
                getNextChar();
                if (peekNextChar() == '/') {
                    fillBuffer();
                } else {
                    System.out.println("Invalid Symbol: /");
                    System.exit(-3);
                }
            }
        }
    }
    
    /** Return the next character in the buffer and move to the next
     *  character. */
    private char getNextChar() {
        char curr = buffer.charAt(currChar);
        currChar += 1;
        return curr;
    }
    
    /** Return the next character in the buffer without moving to the
     *  next */
    private char peekNextChar() {
        return buffer.charAt(currChar);
    }

    public void putTokenBack(Token token) {
        pushBack = token;
    }

    /** Return the next full token in the buffer. */
    public Token nextToken() {
        if (pushBack != null) {
            Token temp = pushBack;
            pushBack = null;
            return temp;
        }
        skipWhitespace();
        stripComments();
        if (endOfFile) {
            return null;
        }
        if (endOfLine()) {
            fillBuffer();
            if (endOfFile) {
                return null;
            }
        }
        StringBuilder builder = new StringBuilder();
        boolean inToken = false;
        while (!endOfLine()) {
            char curr = peekNextChar(); 
            if (Character.isWhitespace(curr)) {
                break;
            }
            if (isToken(curr)) {
                if (!inToken) {
                    builder.append(getNextChar());
                }
                break;
            }
            inToken = true;
            builder.append(getNextChar());
        }
        String text = builder.toString();
        int line = lineNo;
        return new Token(text, fileName, line);
    }

    public Token[] buildTokenArray() {
        Token token;
        ArrayList<Token> result = new ArrayList<>();
        while ((token = nextToken()) != null) {
            result.add(token);
        }
        return result.toArray(new Token[0]);
    }

    public void destroy() {
        try {
            reader.close(); 
        } catch (IOException ex) {
            System.out.println("Tokenizer failed to close file: '" +
                    fileName + "'");
            System.exit(-5);
        }
    }
}
