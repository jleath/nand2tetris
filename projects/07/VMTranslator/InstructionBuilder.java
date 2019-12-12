
public class InstructionBuilder {
    /** The names of all the arithmetic commands in the VM language */
    private static String[] arithmeticCommands = {"add", "sub", "neg", "eq", "gt", "lt", "and", "or", "not"};
    /** The tokenizer will split tokens using only whitespace by default, other delimiters can be specified
     *  by passing a string containing them into the tokenizer's constructor. For VM instructions, we only want
     *  to split on whitespace. */
    private static final String TOKENS = "";
    /** The tokenizer that will be used to generate tokens from the input file
     *  one at a time. */
    private Tokenizer t;
    private String fileName;

    public InstructionBuilder(String fileName) {
        t = new Tokenizer(fileName, TOKENS);
        this.fileName = fileName;
    }

    public void switchFile(String fileName) {
        t.destroy();
        t = new Tokenizer(fileName, TOKENS);
        this.fileName = fileName;
    }

    public String fileName() {
        return fileName;
    }

    public static Instruction sysInitInstruction() {
        return new Instruction(Instruction.CommandType.C_CALL,
                new Token("call", null, 0),
                new Token("Sys.init", null, 0),
                new Token("0", null, 0));
    }

    public static Instruction zeroMemoryInstruction() {
        Token push = new Token("push",null,0);
        Token constant = new Token("constant","",0);
        Token zero = new Token("0", "", 0);
        return new Instruction(Instruction.CommandType.C_PUSH, push, constant, zero);
    }

    public static Instruction gotoFunctionInstruction(String function) {
        Token gotoToken = new Token("goto", null, 0);
        Token nameToken = new Token(function, null, 0);
        return new Instruction(Instruction.CommandType.C_GOTO, gotoToken, nameToken);
    }

    /** Generate the next instruction. Gets the next token, inspects it to determine if
     *  the token is the beginning of an Arithmetic Instruction, a pop instruction or a push instruction.
     */
    public Instruction buildNextInstruction() {
        Token commandToken = t.nextToken();
        if (commandToken == null) {
            return null;
        }
        // All arithmetic commands have no arguments
        if (isArithmeticCommand(commandToken.text())) {
            return new Instruction(Instruction.CommandType.C_ARITHMETIC, commandToken);
        }
        // All pop commands specify a segment (arg1) and an offset (arg2)
        if (commandToken.text().equals("pop")) {
            Token arg1 = t.nextToken();
            Token arg2 = t.nextToken();
            return new Instruction(Instruction.CommandType.C_POP, commandToken, arg1, arg2);
        }
        // All push commands specify a segment (arg1) and an offset (arg2)
        if (commandToken.text().equals("push")) {
            Token arg1 = t.nextToken();
            Token arg2 = t.nextToken();
            return new Instruction(Instruction.CommandType.C_PUSH, commandToken, arg1, arg2);
        }
        // All program flow commands specify a single label argument
        if (commandToken.text().equals("label")) {
            Token arg1 = t.nextToken();
            return new Instruction(Instruction.CommandType.C_LABEL, commandToken, arg1);
        }
        if (commandToken.text().equals("goto")) {
            Token arg1 = t.nextToken();
            return new Instruction(Instruction.CommandType.C_GOTO, commandToken, arg1);
        }
        if (commandToken.text().equals("if-goto")) {
            Token arg1 = t.nextToken();
            return new Instruction(Instruction.CommandType.C_IF, commandToken, arg1);
        }
        if (commandToken.text().equals("function")) {
            Token arg1 = t.nextToken();
            Token arg2 = t.nextToken();
            return new Instruction(Instruction.CommandType.C_FUNCTION, commandToken, arg1, arg2);
        }
        if (commandToken.text().equals("call")) {
            Token arg1 = t.nextToken();
            Token arg2 = t.nextToken();
            return new Instruction(Instruction.CommandType.C_CALL, commandToken, arg1, arg2);
        }
        if (commandToken.text().equals("return")) {
            return new Instruction(Instruction.CommandType.C_RETURN, commandToken);
        }
        return null;
    }

    /** Return true if c is one of the arithmetic commands in the vm language.
     *  add, sub, neg, not, or, and, gt, lt, eq
     */
    private boolean isArithmeticCommand(String c) {
        for (String s : arithmeticCommands) {
            if (c.equals(s)) {
                return true;
            }
        }
        return false;
    }

    /** Close the InstructionBuilder, destroying the tokenizer and closing the file stream */
    public void close() {
        t.destroy();
    }
}
