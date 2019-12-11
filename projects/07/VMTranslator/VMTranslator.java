import java.io.BufferedWriter;
import java.io.FileWriter;
import java.util.ArrayList;

public class VMTranslator {

    private static final String DELIMITERS = "";

    public static void main(String[] args) {
        /* Check inputs and determine what the output file should be named. */
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
            output = fileName.substring(0, fileName.length()-3) + ".asm";
        }


        /* Create an instruction builder to parse the assembly code and generate
         *  Hack machine code instructions.
         */
        InstructionBuilder builder = new InstructionBuilder(fileName);
        ArrayList<Instruction> instructions = new ArrayList<>();
        Instruction instr;
        /* Generate each instruction one at a time and add to the Instruction list */
        do {
            instr = builder.buildNextInstruction();
            instructions.add(instr);
        }
        while (instr != null);
        builder.close();
        /* Feed all the instructions into a CodeGenerator and append the output for each instruction
           to a StringBuilder
         */
        StringBuilder outputBuilder = new StringBuilder();
        CodeGenerator codeGen = new CodeGenerator(fileName);
        for (Instruction i : instructions) {
            if (i == null) {
                break;
            }
            String generated = codeGen.generateCode(i);
            outputBuilder.append(generated);
        }
        /* All assembly programs should end with an infinite loop.
              (NOEND)
              @NOEND
              0;JMP
         */
        outputBuilder.append(codeGen.infiniteLoop());

        /* Attempt to write to the output file. If this fails, just print an error message and allow the
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
