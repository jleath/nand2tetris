import java.io.BufferedWriter;
import java.io.FileWriter;
import java.util.LinkedList;

public class HackAssembler {

    private static final String NEWLINE = "\n";

    public static void main(String[] args) {
        /** Check inputs and determine what the output file should be named. */
        if (args.length < 1) {
            System.out.println("Error: Input file argument required");
            System.out.println();
            System.exit(-4);
        }
        String fileName = args[0];
        String output;
        if (args.length > 1) {
            output = args[1]; 
        } else {
            output = fileName.substring(0, fileName.length()-4) + ".hack";
        }

        SymbolTable symbols = new SymbolTable();

        /** Create an instruction builder to parse the assembly code and generate
         *  Hack machine code instructions.
         */
        InstructionBuilder builder = new InstructionBuilder(fileName);
        LinkedList<Instruction> instructions = new LinkedList<>();
        Instruction instr;
        /** Generate each instruction one at a time and append to the StringBuilder object. */
        do {
            instr = builder.buildNextInstruction();
            instructions.add(instr);
        }
        while (instr != null);

        CodeGenerator codeGen = new CodeGenerator();
        codeGen.buildLabels(instructions, symbols);
        StringBuilder outputBuilder = new StringBuilder();

        for (Instruction i : instructions) {
            String generated = codeGen.generateInstruction(i, symbols);
            outputBuilder.append(generated);
            outputBuilder.append(NEWLINE);
        }

        /** Attempt to write to the output file. If this fails, just print an error message and allow the
         *  program to end.
         */
        try {
            BufferedWriter writer = new BufferedWriter(new FileWriter(output));
            writer.write(outputBuilder.toString());
            writer.close();
        } catch (Exception ex) {
            System.out.println("Failed to write to '" + output + "'");
        }
    }
}
