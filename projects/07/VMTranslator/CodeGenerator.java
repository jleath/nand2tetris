public class CodeGenerator {
    private static final String UNCONDITIONAL_JUMP_LABEL = "0;JMP\n";
    private static final String DECREMENT_SP = "@SP\nM=M-1\nA=M\n";
    private static final String INCREMENT_SP = "@SP\nM=M+1\n";
    private static final String STORE_D_IN_SP = "@SP\nA=M\nM=D\n";
    private static final String NEG_CODE = "M=-M\n";
    private static final String NOT_CODE = "M=!M\n";
    private static final String ADD_CODE = "M=M+D\n";
    private static final String SUB_CODE = "M=M-D\n";
    private static final String AND_CODE = "M=M&D\n";
    private static final String OR_CODE = "M=M|D\n";
    private static final String SET_TRUE = "@SP\nA=M\nM=-1\n";
    private static final String SET_FALSE = "@SP\nA=M\nM=0\n";
    private static final String POP_COPY_CODE = "@tmp\nM=D\n@SP\nM=M-1\nA=M\nD=M\n@tmp\nA=M\nM=D\n";
    private static final String PUSH_COPY_CODE = "D=M\n@SP\nA=M\nM=D\n" + INCREMENT_SP;
    private static final String[] SEGMENT_NAMES = {"local", "argument", "this", "that"};
    private static final String[] SEGMENT_CODES = {"LCL", "ARG", "THIS", "THAT"};
    private static final String INFINITE_JUMP_LABEL = "NOEND";
    private static final String COMMENT_DELIMITER = "// ";
    private static final String INPUT_FILE_FORMAT = ".vm";
    private static final String FUNC_RETURN_LABEL = "FUNC_RETURN_";
    private static final int TEMP_BASE_ADDRESS = 5;
    private static final int POINTER_BASE_ADDRESS = 3;

    private int numJumpNames;
    private int numReturnNames;
    private String fileName;
    private String currFunctionName;

    public CodeGenerator(String fileName) {
        numJumpNames = 0;
        numReturnNames = 0;
        this.fileName = removePaths(fileName);
        currFunctionName = "";
    }

    /** Extract and return the name of a file from a path. */
    private String removePaths(String file) {
        int i = file.length() - 1;
        while (i >= 0) {
            char curr = file.charAt(i);
            // Handle both linux and windows path separators
            if (curr == '/' || curr == '\\') {
                file = file.substring(i+1);
                break;
            }
            i -= 1;
        }
        return file.substring(0, file.length()-INPUT_FILE_FORMAT.length());
    }

    /** For each instruction processed, include a comment listing the original vm instruction */
    private String buildComment(Instruction i) {
        return COMMENT_DELIMITER + i.toString() + " (" + i.command().details() + ")\n";
    }

    private String getSegmentCode(String segment) {
        int i = 0;
        while (i < SEGMENT_NAMES.length) {
            if (SEGMENT_NAMES[i].equals(segment)) {
                return SEGMENT_CODES[i];
            }
            i += 1;
        }
        return null;
    }

    public String generateCode(Instruction i) {
        if (i.command().fileName() != null && ! i.command().fileName().equals(fileName)) {
            fileName = i.command().fileName();
        }
        String comment = buildComment(i);
        if (i.commandType() == Instruction.CommandType.C_ARITHMETIC) {
            return comment + generateArithmetic(i);
        } else if (i.commandType() == Instruction.CommandType.C_PUSH) {
            return comment + generatePush(i);
        } else if (i.commandType() == Instruction.CommandType.C_POP) {
            return comment + generatePop(i);
        } else if (i.commandType() == Instruction.CommandType.C_LABEL) {
            return comment + generateLabel(i.arg1().text());
        } else if (i.commandType() == Instruction.CommandType.C_GOTO) {
            return comment + generateGoto(i);
        } else if (i.commandType() == Instruction.CommandType.C_IF) {
            return comment + generateIf(i);
        } else if (i.commandType() == Instruction.CommandType.C_FUNCTION) {
            return comment + generateFunction(i);
        } else if (i.commandType() == Instruction.CommandType.C_RETURN) {
            return comment + generateReturn(i);
        } else if (i.commandType() == Instruction.CommandType.C_CALL) {
            return comment + generateCall(i);
        } else {
            return "";
        }
    }

    /** Return the machine code for a jump label within a function */
    private String generateLabel(String label) {
        return buildJumpLabel(buildFunctionLabel(label));
    }

    /** Return the machine code needed to affect a goto command. */
    private String generateGoto(Instruction i) {
        return buildAInstruction(buildFunctionLabel(i.arg1().text())) + UNCONDITIONAL_JUMP_LABEL;
    }

    /** Return the machine code needed to affect an if-goto command. */
    private String generateIf(Instruction i) {
        String label = buildFunctionLabel(i.arg1().text());
        return DECREMENT_SP + "D=M\n" + buildAInstruction(label) + "D;JNE\n";
    }

    public String generateInit() {
        Instruction callSysInit = new Instruction(Instruction.CommandType.C_CALL,
                                                    new Token("call", null, 0),
                                                    new Token("Sys.init",null,0),
                                                    new Token("0", null, 0));
        return "@256\nD=A\n@SP\nM=D\n" + generateCode(callSysInit);
    }

    public String generateFunction(Instruction i) {
        StringBuilder builder = new StringBuilder();
        builder.append(generateLabel(i.arg1().text()));
        Token push = new Token("push","",0);
        Token constant = new Token("constant","",0);
        Token zero = new Token("0", "", 0);
        Instruction zeroMemory = new Instruction(Instruction.CommandType.C_PUSH, new Token("push", null, 0), constant, zero);
        for (int j = 0; j < Integer.parseInt(i.arg2().text()); j++) {
            builder.append(generateCode(zeroMemory));
        }
        return builder.toString();
    }

    public String generateCall(Instruction i) {
        String returnName = getNextReturnName();
        String functionName = i.arg1().text();
        String n = i.arg2().text();
        Instruction gotoF = new Instruction(Instruction.CommandType.C_GOTO, new Token("goto", null, 0), new Token(functionName, null, 0));
        // push return address
        String result = "@" + returnName +"\nD=A\n@SP\nA=M\nM=D\n@SP\nM=M+1\n";
        // save state
        result += "@LCL\nD=M\n@SP\nA=M\nM=D\n@SP\nM=M+1\n";
        result += "@ARG\nD=M\n@SP\nA=M\nM=D\n@SP\nM=M+1\n";
        result += "@THIS\nD=M\n@SP\nA=M\nM=D\n@SP\nM=M+1\n";
        result += "@THAT\nD=M\n@SP\nA=M\nM=D\n@SP\nM=M+1\n";
        // reposition ARG pointer
        result += "@SP\nD=M\n@" + n + "\nD=D-A\n@5\nD=D-A\n@ARG\nM=D\n";
        // reposition LCL pointer
        result += "@SP\nD=M\n@LCL\nM=D\n";
        // execute function
        result += generateCode(gotoF);
        // return label
        result += buildJumpLabel(returnName);
        return result;
    }

    public String generateReturn(Instruction i) {
        Instruction popToArg = new Instruction(Instruction.CommandType.C_POP, new Token("pop", null, 0), new Token("argument", null, 0),
                                               new Token("0", null, 0));
        // Store the current value of LCL
        String result = "@LCL\nD=M\n@FRAME\nM=D\n";
        // store the return address
        result += "@5\nD=A\n@FRAME\nD=M-D\nA=D\nD=M\n@RET\nM=D\n";
        // reposition return value
        result += generateCode(popToArg);
        // reset SP
        result += "@ARG\nD=M\nD=D+1\n@SP\nM=D\n";
        // reset saved state
        result += "@1\nD=A\n@FRAME\nD=M-D\nA=D\nD=M\n@THAT\nM=D\n";
        result += "@2\nD=A\n@FRAME\nD=M-D\nA=D\nD=M\n@THIS\nM=D\n";
        result += "@3\nD=A\n@FRAME\nD=M-D\nA=D\nD=M\n@ARG\nM=D\n";
        result += "@4\nD=A\n@FRAME\nD=M-D\nA=D\nD=M\n@LCL\nM=D\n";
        // goto return
        return result + "@RET\nA=M\n0;JMP\n";
    }


    /** Generate the hack machine code needed to affect a pop operation.
     *  Arg1 of the instruction I is the destination memory segment,
     *  Arg2 is the offset into the memory segment where the data should be placed.
     *  Returns a string representing the machine code.
     */
    private String generatePop(Instruction i) {
        String segment = i.arg1().text();
        String offset = i.arg2().text();
        // Handle special case where we are using the temp memory segment, this is
        // a special case because we are directly accessing a memory location rather
        // than using a built-in symbol in the Hack language
        if (segment.equals("temp")) {
            int address = TEMP_BASE_ADDRESS + Integer.parseInt(offset);
            return generateDirectPop(Integer.toString(address));
        }
        // Handle special case where we are using the pointer memory segment, this is
        // similar to the "temp" case
        if (segment.equals("pointer")) {
            int address = POINTER_BASE_ADDRESS + Integer.parseInt(offset);
            return generateDirectPop(Integer.toString(address));
        }
        // Handle special case where we are using the static memory segment. This is
        // a special case because static data gets a special label ('fileName.offset')
        if (segment.equals("static")) {
            String staticLabel = buildStaticLabel(offset);
            return generateDirectPop(staticLabel);
        }
        String segmentCode = getSegmentCode(segment);
        if (segmentCode == null) {
            throw new RuntimeException("Invalid segment name: pop " + segment + " " + offset);
        } else {
            return generateBasicPop(segmentCode, offset);
        }
    }

    /** Returns a string representing a label in the hack machine language ('@label') */
    private String buildAInstruction(String label) {
        return "@" + label + "\n";
    }

    /** Returns a string representing a static symbol name in the hack machine language ('fileName.offset') */
    private String buildStaticLabel(String offset) {
        String name = removePaths(fileName);
        return name + "." + offset;
    }

    /** Returns a string with the hack machine code needed to affect a simple pop from the stack into
     *  *(*(segmentCode)+offset) = Stack[--SP]
     */
    private String generateBasicPop(String segmentCode, String offset) {
        String offsetLabel = buildAInstruction(offset);
        String segmentLabel = buildAInstruction(segmentCode);
        return offsetLabel + "D=A\n" + segmentLabel + "D=D+M\n" + POP_COPY_CODE;
    }

    /** Similar to generateBasicPop, except takes a direct address instead of a segmentCode
     *  and offset. Used for accessing the temp and pointer memory segments
     */
    private String generateDirectPop(String address) {
        String addressLabel = buildAInstruction(address);
        return addressLabel + "D=A\n" + POP_COPY_CODE;
    }

    /** Returns a string with the hack machine code needed to affect a simple push onto the stack
     *  using the data located at the given offset of the given memory segment.
     *  *(*SP++) = *(*(segmentCode)+offset))
     */
    private String generatePush(Instruction i) {
        String segment = i.arg1().text();
        String offset = i.arg2().text();
        String offsetLabel = buildAInstruction(offset);
        if (segment.equals("constant")) {
            return offsetLabel + "D=A\n" + STORE_D_IN_SP + INCREMENT_SP;
        }
        return generatePush(segment, offset);
    }

    /** Dispatch method for handling push operations */
    private String generatePush(String segment, String offset) {
        if (segment.equals("temp")) {
            int address = Integer.parseInt(offset) + 5;
            return generateDirectPush(Integer.toString(address));
        }
        if (segment.equals("pointer")) {
            int address = Integer.parseInt(offset) + 3;
            return generateDirectPush(Integer.toString(address));
        }
        if (segment.equals("static")) {
            String staticLabel = buildStaticLabel(offset);
            return generateDirectPush(staticLabel);
        }
        String segmentCode = getSegmentCode(segment);
        if (segmentCode == null) {
            throw new RuntimeException("Invalid segment name in push operation: push " + segment + " " + offset);
        } else {
            return generateBasicPush(segmentCode, offset);
        }
    }

    private String generateBasicPush(String segmentCode, String offset) {
        String offsetLabel = buildAInstruction(offset);
        String segmentLabel = buildAInstruction(segmentCode);
        return offsetLabel + "D=A\n" + segmentLabel + "D=M+D\nA=D\n" + PUSH_COPY_CODE;
    }

    private String generateDirectPush(String address) {
        String addressLabel = buildAInstruction(address);
        return addressLabel + PUSH_COPY_CODE;
    }

    private String getNextJumpName() {
        numJumpNames += 1;
        return "JUMP" + numJumpNames;
    }

    private String generateArithmetic(Instruction i) {
        // We will always start an arithmetic operation by decrementing the
        // stack pointer at least one time
        String commandText = i.command().text();
        String result = DECREMENT_SP;
        if (commandText.equals("neg")) {
            return result + NEG_CODE + INCREMENT_SP;
        } else if (commandText.equals("not")) {
            return result + NOT_CODE + INCREMENT_SP;
        }

        // For all the binary arithmetic operations, we know we will have to
        // store the top item on the stack in the D register
        result = result + "D=M\n" + DECREMENT_SP;

        if (commandText.equals("add")) {
            return result + ADD_CODE + INCREMENT_SP;
        } else if (commandText.equals("sub")) {
            return result + SUB_CODE + INCREMENT_SP;
        } else if (commandText.equals("eq")) {
            return result + buildConditional("JEQ") + INCREMENT_SP;
        } else if (commandText.equals("gt")) {
            return result + buildConditional("JGT") + INCREMENT_SP;
        } else if (commandText.equals("lt")) {
            return result + buildConditional("JLT") + INCREMENT_SP;
        } else if (commandText.equals("and")) {
            return result + AND_CODE + INCREMENT_SP;
        } else if (commandText.equals("or")) {
            return result + OR_CODE + INCREMENT_SP;
        }
        return "";
    }

    private String buildConditional(String jumpCode) {
        String jumpLabel1 = getNextJumpName();
        String jumpLabel2 = getNextJumpName();
        String result = "D=M-D\n" + buildAInstruction(jumpLabel1);
        result = result + "D;" + jumpCode + "\n";
        result = result + SET_FALSE;
        result = result + unconditionalJump(jumpLabel2);
        result = result + buildJumpLabel(jumpLabel1);
        result = result + SET_TRUE;
        result = result + unconditionalJump(jumpLabel2);
        return result + buildJumpLabel(jumpLabel2);
    }

    private String unconditionalJump(String label) {
        return buildAInstruction(label) + UNCONDITIONAL_JUMP_LABEL;
    }

    private String buildFunctionLabel(String label) {
        return currFunctionName + "$" + label;
    }

    private String buildJumpLabel(String label) {
        return "(" + label + ")\n";
    }

    public String infiniteLoop() {
        return buildJumpLabel(INFINITE_JUMP_LABEL) + unconditionalJump(INFINITE_JUMP_LABEL);
    }

    private String getNextReturnName() {
        numReturnNames += 1;
        return FUNC_RETURN_LABEL + numReturnNames;
    }

}

