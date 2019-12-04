import java.util.HashMap;

public class InstructionBuilder {
    /** The first character of an A instruction in the assembly code. */
    private static final char A_INSTRUCTION_FLAG = '@';
    /** The first character of an L instruction in the assembly code. */
    private static final char L_INSTRUCTION_FLAG = '(';
    /** The tokenizer that will be used to generate tokens from the input file
     *  one at a time. */
    private Tokenizer t;
    /** The number of instructions that have been generated so far. */
    private int numInstructions;
    /** The name of the input file. */
    private String fileName;
    /** A symbol table holding a mapping of all symbols and labels. Also
     *  stores the mapping from Assembly mnemonics to Binary machine code sequences. */
    private SymbolTable symbols;

    public InstructionBuilder(String fileName) {
        t = new Tokenizer(fileName);
        numInstructions = 0;
        this.fileName = fileName;
        symbols = new SymbolTable();
        buildLabels();
    }

    /** First pass through the assembly code, the only operation that will be
     *  completed as instructions are generated is to find goto labels and
     *  map them to the correct instruction address in the symbol table.
     */
    private void buildLabels() {
        String instruction = "";
        while (instruction != null) {
            instruction = buildInstruction(true);
        }
        /** Reset the tokenizer */
        t.destroy();
        t = new Tokenizer(fileName);
        numInstructions = 0;
    }

    /** Returns true if TOKEN is the beginning of an A instruction */
    private boolean isAInstruction(String token) {
        return token.charAt(0) == A_INSTRUCTION_FLAG;
    }

    /** Returns true if TOKEN is the beginning of an L instruction. */
    private boolean isLInstruction(String token) {
        return token.charAt(0) == L_INSTRUCTION_FLAG;
    }

    /** Returns the name of the label that TOKEN represents
     *  ex. (LABEL) -> LABEL
     */
    private String extractLabel(String token) {
        return token.substring(1, token.length()-1);
    }

    public String buildInstruction() {
        return buildInstruction(false);
    }

    /** Generate the next instruction. Gets the next token, inspects it to determine if
     *  the token is the beginning of an L instruction, A instruction, C Instruction.
     *  If the instruction is an L instruction and labelPass is true, we will just add the label
     *  to the symbol table. Otherwise we skip the token entirely.
     */
    public String buildInstruction(boolean labelPass) {
        Token token = t.nextToken();
        if (token == null) {
            return null;
        }
        if (isAInstruction(token.text())) {
            numInstructions += 1;
            t.putTokenBack(token);
            return buildAInstruction(labelPass);    
        } else if (isLInstruction(token.text())) {
            if (labelPass) {
                symbols.addSymbol(extractLabel(token.text()), Integer.toString(numInstructions));
            }
            return buildInstruction(labelPass);
        } else {
            numInstructions += 1;
            t.putTokenBack(token);
            return buildCInstruction();
        }
    }

    /** Adds leading 0's to the contents of A until A is 16 characters long.
     *  Used to pad out addresses to make sure all machine code instructions are 16 bits.
     */
    private String expandAddress(String a) {
        int len = a.length();
        int toFill = 16 - len;
        StringBuilder b = new StringBuilder();
        for (int i = 0; i < toFill; i++) {
            b.append("0");
        }
        b.append(a);
        return b.toString();
    }

    /** Generate an A instruction. If the token following the @ symbol is a number,
     *  return a binary translation of that number.
     *  Otherwise, if labelPass is true, do nothing and return an empty string.
     *  If labelPass is false, we need to look up the value of the label in order
     *  to generate the A instruction.
     *
     *  If this method returns an address, the addres must be converted to Binary and
     *  padded with leading 0's to ensure the result is 16 bits wide.
     */
    public String buildAInstruction(boolean labelPass) {
        Token token = t.nextToken();
        token.setText(token.text().substring(1));
        if (Character.isDigit(token.text().charAt(0))) {
            return expandAddress(Integer.toBinaryString(Integer.parseInt(token.text())));
        }
        if (!labelPass) {
            String value = symbols.getValue(token.text());
            return expandAddress(Integer.toBinaryString(Integer.parseInt(value)));
        }
        return "";
    }

    private Token copyToken(Token t1) {
        return new Token(t1.text(), t1.fileName(), t1.lineNumber());
    }

    /** Returns a C instruction. C Instructions can come in one of the following forms:
     *  - dest=comp;jmp
     *  - dest=comp
     *  - comp;jmp
     *  - comp
     *  To determine which form the next instruction must be, we inspect tokens one at
     *  a time, putting them back into the Tokenizer as needed. Once we have determined
     *  the instruction form and the text used for the dest, comp and jump fields, we can
     *  look them up in the symbol table to get their corresponding machine code values.
     *  If this lookup fails, we assume that the assembly instruction was malformed and
     *  print an error message that shows the filename, line number and which field was
     *  not able to be found in the symbol table.
     */
    public String buildCInstruction() {
        Token dest = new Token("null", null, 0);
        Token comp = new Token("null", null, 0);
        Token jump = new Token("null", null, 0);
        Token token1 = t.nextToken();
        Token op1 = t.nextToken();
        if (op1 != null && op1.text().equals("=")) {
            dest = copyToken(token1);
            comp = copyToken(t.nextToken());
            Token op2 = t.nextToken();
            // instruction is of form dest=comp;jump
            if (op2 != null && op2.text().equals(";")) {
                jump = copyToken(t.nextToken());
            // instruction is of form dest=comp
            } else {
                t.putTokenBack(op2);
            }
        // instruction is of form comp;jump
        } else if (op1 != null && op1.text().equals(";")) {
            comp = copyToken(token1);
            jump = copyToken(t.nextToken());
        } else {
            // instruction is of form comp
            comp = copyToken(token1);
            t.putTokenBack(op1);
        }
        try {
            String compCode = symbols.getCompCode(comp);
            String destCode = symbols.getDestCode(dest);
            String jumpCode = symbols.getJumpCode(jump);
            return "111" + compCode + destCode + jumpCode;
        } catch (RuntimeException ex) {
            System.out.println(ex.getMessage());
            System.exit(-7);
            return null;
        }
    }
}
