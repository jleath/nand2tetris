import java.io.BufferedWriter;
import java.io.FileWriter;

public class HackAssembler {

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

        /** Create an instruction builder to parse the assembly code and generate
         *  Hack machine code instructions.
         */
        InstructionBuilder builder = new InstructionBuilder(fileName);
        /**
         * Using a stringbuilder object to store the entire set of instructions in one string,
         * output to the file will be performed as one write.
         */
        StringBuilder instruction = new StringBuilder();
        final String newline = "\n";
        String instr = "";
        /** Generate each instruction one at a time and append to the StringBuilder object. */
        while (instr != null) {
            instr = builder.buildInstruction();
            if (instr != null) {
                /** Append a new line as well */
                instruction.append(instr + "\n");
            }
        }
        /** Attempt to write to the output file. If this fails, just print an error message and allow the
         *  program to end.
         */
        try {
            BufferedWriter writer = new BufferedWriter(new FileWriter(output));
            writer.write(instruction.toString());
            writer.close();
        } catch (Exception ex) {
            System.out.println("Failed to write to '" + output + "'");
        }
    }
}
