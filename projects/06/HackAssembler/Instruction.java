public class Instruction {

    public enum InstructionType {
        C_INSTRUCTION, A_INSTRUCTION, L_INSTRUCTION
    }
    private InstructionType type;
    private Token label;
    private Token address;
    private Token dest;
    private Token comp;
    private Token jump;

    public Instruction(InstructionType type, Token token) {
        label = null;
        address = null;
        dest = null;
        comp = null;
        jump = null;
        this.type =type;
        if (type == InstructionType.A_INSTRUCTION) {
            address = token;
        } else if (type == InstructionType.L_INSTRUCTION) {
            label = token;
        } else {
            throw new RuntimeException("Invalid parameters for constructing a C Instruction object.");
        }
    }

    public Instruction(InstructionType type, Token dest, Token comp, Token jump) {
        if (type != InstructionType.C_INSTRUCTION) {
            throw new RuntimeException("Invalid parameters for constructing an L or A instruction object.");
        }
        label = null;
        address = null;
        this.type = type;
        this.dest = dest;
        this.jump = jump;
        this.comp = comp;
    }

    public Token label() {
        if (type != InstructionType.L_INSTRUCTION) {
            throw new RuntimeException("Attempt to get a label from a C or A instruction.");
        }
        return label;
    }

    public Token address() {
        if (type != InstructionType.A_INSTRUCTION) {
            throw new RuntimeException("Attempt to get an address from an L or C instruction.");
        }
        return address;
    }

    public Token dest() {
        if (type != InstructionType.C_INSTRUCTION) {
            throw new RuntimeException("Attempt to get a dest field from an A or L instruction.");
        }
        return dest;
    }

    public Token jump() {
        if (type != InstructionType.C_INSTRUCTION) {
            throw new RuntimeException("Attempt to get a jump field from an A or L instruction.");
        }
        return jump;
    }

    public Token comp() {
        if (type != InstructionType.C_INSTRUCTION) {
            throw new RuntimeException("Attempt to get a comp field from an A or L instruction.");
        }
        return comp;
    }
}
