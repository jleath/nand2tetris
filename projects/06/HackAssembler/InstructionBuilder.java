import java.util.HashMap;

public class InstructionBuilder {
    /** The first character of an A instruction in the assembly code. */
    private static final char A_INSTRUCTION_FLAG = '@';
    /** The first character of an L instruction in the assembly code. */
    private static final char L_INSTRUCTION_FLAG = '(';
    private static final String TOKENS = ";=";
    /** The tokenizer that will be used to generate tokens from the input file
     *  one at a time. */
    private Tokenizer t;

    public InstructionBuilder(String fileName) {
        t = new Tokenizer(fileName, TOKENS);
    }

    /** Returns true if TOKEN is the beginning of an A instruction */
    private boolean isAInstruction(String token) {
        return token.charAt(0) == A_INSTRUCTION_FLAG;
    }

    /** Returns true if TOKEN is the beginning of an L instruction. */
    private boolean isLInstruction(String token) {
        return token.charAt(0) == L_INSTRUCTION_FLAG;
    }

    /** Generate the next instruction. Gets the next token, inspects it to determine if
     *  the token is the beginning of an L instruction, A instruction, C Instruction.
     *  If the instruction is an L instruction and labelPass is true, we will just add the label
     *  to the symbol table. Otherwise we skip the token entirely.
     */
    public Instruction buildNextInstruction() {
        Token token = t.nextToken();
        if (token == null) {
            return null;
        }
        if (isAInstruction(token.text())) {
            return new Instruction(Instruction.InstructionType.A_INSTRUCTION, token);
        } else if (isLInstruction(token.text())) {
            return new Instruction(Instruction.InstructionType.L_INSTRUCTION, token);
        } else {
            t.putTokenBack(token);
            return buildCInstruction();
        }
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
    public Instruction buildCInstruction() {
        Token dest = new Token("null", null, 0);
        Token comp = new Token("null", null, 0);
        Token jump = new Token("null", null, 0);
        Token token1 = t.nextToken();
        Token op1 = t.nextToken();
        if (op1 != null && op1.text().equals("=")) {
            dest = token1;
            comp = t.nextToken();
            Token op2 = t.nextToken();
            // instruction is of form dest=comp;jump
            if (op2 != null && op2.text().equals(";")) {
                jump = t.nextToken();
            // instruction is of form dest=comp
            } else {
                t.putTokenBack(op2);
            }
        // instruction is of form comp;jump
        } else if (op1 != null && op1.text().equals(";")) {
            comp = token1;
            jump = t.nextToken();
        } else {
            // instruction is of form comp
            comp = token1;
            t.putTokenBack(op1);
        }
        return new Instruction(Instruction.InstructionType.C_INSTRUCTION, dest, comp, jump);
    }
}
