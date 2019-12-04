import java.util.ArrayList;
import java.util.List;

public class CodeGenerator {
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

    public String generateInstruction(Instruction i, SymbolTable symbols) {
        if (i == null) {
            return "";
        }
        if (i.isAInstruction()) {
            return generateAInstruction(i, symbols);
        } else if (i.isCInstruction()) {
            return generateCInstruction(i, symbols);
        } else {
            throw new RuntimeException("Invalid instruction passed to generateInstruction -> " + i);
        }
    }

    /** Generate an A instruction. If the token following the @ symbol is a number,
     *  return a binary translation of that number.
     *
     *  If this method returns an address, the address must be converted to Binary and
     *  padded with leading 0's to ensure the result is 16 bits wide.
     */
    private String generateAInstruction(Instruction i, SymbolTable symbols) {
        if (! i.isAInstruction()) {
            throw new RuntimeException("Invalid instruction passed to generateAInstruction -> " + i);
        }
        Token t = i.address();
        String address = t.text().substring(1);
        String value = address;
        if (! Character.isDigit(address.charAt(0))) {
            value = symbols.getValue(address);
        }
        return expandAddress(Integer.toBinaryString(Integer.parseInt(value)));
    }

    private String generateCInstruction(Instruction i, SymbolTable symbols) {
        if (! i.isCInstruction()) {
            throw new RuntimeException("Invalid instruction passed to generateCInstruction -> " + i);
        }
        Token comp = i.comp();
        Token dest = i.dest();
        Token jump = i.jump();
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

    private String extractLabel(Token t) {
        String text = t.text();
        return text.substring(1, text.length()-1);
    }

    public void buildLabels(List<Instruction> instructions, SymbolTable symbols) {
        int i = 0;
        while (i < instructions.size()) {
            Instruction instr = instructions.get(i);
            if (instr == null) {
                break;
            }
            if (instr.isLInstruction()) {
                String label = extractLabel(instr.label());
                symbols.addSymbol(label, Integer.toString(i));
                instructions.remove(i);
            } else {
                i += 1;
            }
        }
    }
}
