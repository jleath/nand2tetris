public class Instruction {
    /**
     * VM Instructions can be one of 9 types. The CommandType enum specifies these types.
     */
    public enum CommandType {
        C_ARITHMETIC, C_PUSH, C_POP, C_LABEL, C_GOTO, C_IF, C_FUNCTION, C_RETURN, C_CALL
    }

    ;

    /**
     * The first token of the instruction. eg. add, sub, push, pop
     */
    private Token command;
    /**
     * The second token of the instruction, the memory segment to operate on.
     */
    private Token arg1;
    /**
     * The third token of the instruction, the offset to use when accessing a memory segment
     */
    private Token arg2;
    /**
     * The type of the instruction, dictated by the command variable.
     */
    private CommandType commandType;

    /**
     * Constructor for single argument commands. eg. goto label
     */
    public Instruction(CommandType type, Token c, Token arg1) {
        this(type, c, arg1, null);
    }

    /** Constructor for arithmetic commands */
    public Instruction(CommandType type, Token c) {
        this(type, c, null, null);
    }

    /** general constructor for an instruction with one command and two parameters */
    public Instruction(CommandType type, Token c, Token a1, Token a2) {
        commandType = type;
        command = c;
        arg1 = a1;
        arg2 = a2;
    }

    public CommandType commandType() {
        return commandType;
    }

    public Token command() {
        return command;
    }

    public Token arg1() {
        return arg1;
    }

    public Token arg2() {
        return arg2;
    }

    public String toString() {
        String c = command.text();
        String a1, a2;
        if (arg1 == null) {
            a1 = "";
        } else {
            a1 = arg1.text();
        }
        if (arg2 == null) {
            a2 = "";
        } else {
            a2 = arg2.text();
        }
        return c + " " + a1 + " " + a2;
    }
}
