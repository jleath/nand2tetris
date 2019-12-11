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
    private static final String PUSH_COPY_CODE = "\nD=M\n@SP\nA=M\nM=D\n" + INCREMENT_SP;
    private static final String[] SEGMENT_NAMES = {"local", "argument", "this", "that"};
    private static final String[] SEGMENT_CODES = {"LCL", "ARG", "THIS", "THAT"};
    private static final String INFINITE_JUMP_LABEL = "NOEND";
    private static final String COMMENT_DELIMITER = "// ";
    private static final String INPUT_FILE_FORMAT = ".vm";
    private static final int TEMP_BASE_ADDRESS = 5;
    private static final int POINTER_BASE_ADDRESS = 3;

    private int numJumpLabels;
    private String fileName;

    public CodeGenerator(String fileName) {
        numJumpLabels = 0;
        this.fileName = removePaths(fileName);
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
        return COMMENT_DELIMITER + i.toString() + "\n";
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
        String comment = buildComment(i);
        if (i.commandType() == Instruction.CommandType.C_ARITHMETIC) {
            return comment + generateArithmetic(i);
        } else if (i.commandType() == Instruction.CommandType.C_PUSH) {
            return comment + generatePush(i);
        } else if (i.commandType() == Instruction.CommandType.C_POP) {
            return comment + generatePop(i);
        } else if (i.commandType() == Instruction.CommandType.C_LABEL) {
            return comment + generateLabel(i);
        } else if (i.commandType() == Instruction.CommandType.C_GOTO) {
            return comment + generateGoto(i);
        } else if (i.commandType() == Instruction.CommandType.C_IF) {
            return comment + generateIf(i);
        } else {
            return "";
        }
    }

    private String generateLabel(Instruction i) {
        // TODO
        return null;
    }

    private String generateGoto(Instruction i) {
        // TODO
        return null;
    }

    private String generateIf(Instruction i) {
        // TODO
        return null;
    }

    private String generatePop(Instruction i) {
        String segment = i.arg1().text();
        String offset = i.arg2().text();
        if (segment.equals("temp")) {
            int address = TEMP_BASE_ADDRESS + Integer.parseInt(offset);
            return generateDirectPop(Integer.toString(address));
        }
        if (segment.equals("pointer")) {
            int address = POINTER_BASE_ADDRESS + Integer.parseInt(offset);
            return generateDirectPop(Integer.toString(address));
        }
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

    private String buildAInstruction(String label) {
        return "@" + label + "\n";
    }

    private String buildStaticLabel(String offset) {
        return fileName + "." + offset;
    }

    private String generateBasicPop(String segmentCode, String offset) {
        String offsetLabel = buildAInstruction(offset);
        String segmentLabel = buildAInstruction(segmentCode);
        return offsetLabel + "D=A\n" + segmentLabel + "D=D+M\n" + POP_COPY_CODE;
    }

    private String generateDirectPop(String address) {
        String addressLabel = buildAInstruction(address);
        return addressLabel + "D=A\n" + POP_COPY_CODE;
    }

    private String generatePush(Instruction i) {
        String segment = i.arg1().text();
        String offset = i.arg2().text();
        String offsetLabel = buildAInstruction(offset);
        if (segment.equals("constant")) {
            return offsetLabel + "D=A\n" + STORE_D_IN_SP + INCREMENT_SP;
        }
        return generatePush(segment, offset);
    }

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

    private String getNextJumpLabel() {
        numJumpLabels += 1;
        return "JUMP" + numJumpLabels;
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
        String jumpLabel1 = getNextJumpLabel();
        String jumpLabel2 = getNextJumpLabel();
        String result = "D=M-D\n" + buildAInstruction(jumpLabel1);
        result = result + "D;" + jumpCode + "\n";
        result = result + SET_FALSE;
        result = result + unconditionalJump(jumpLabel2);
        result = result + buildLabel(jumpLabel1);
        result = result + SET_TRUE;
        result = result + unconditionalJump(jumpLabel2);
        return result + buildLabel(jumpLabel2);
    }

    private String unconditionalJump(String label) {
        return buildAInstruction(label) + UNCONDITIONAL_JUMP_LABEL;
    }

    private String buildLabel(String label) {
        return "(" + label + ")\n";
    }

    public String infiniteLoop() {
        return buildLabel(INFINITE_JUMP_LABEL) + unconditionalJump(INFINITE_JUMP_LABEL);
    }

}

