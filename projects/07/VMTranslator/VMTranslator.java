import java.io.BufferedWriter;
import java.io.FileWriter;
import java.util.ArrayList;
import java.io.File;
import java.util.List;

public class VMTranslator {

    private static final String VM_FILE_EXTENSION = ".vm";

    private static ArrayList<String> buildFileList(File file) {
        ArrayList<String> files = new ArrayList<>();
        if (file.isFile()) {
            files.add(file.getAbsolutePath());
        } else if (file.isDirectory()) {
            for (String name : file.list()) {
                if (name.substring(name.length() - VM_FILE_EXTENSION.length()).equals(VM_FILE_EXTENSION)) {
                    files.add(file.getAbsolutePath() + "/" + name);
                }
            }
        }
        return files;
    }

    private static String getOutputFilename(File file) {
        if (file.isFile()) {
            String name = file.getAbsolutePath();
            return name.substring(0, name.length() - VM_FILE_EXTENSION.length()) + ".asm";
        } else {
            String name = file.getName();
            return file.getAbsolutePath() + "/" + name + ".asm";
        }
    }

    public static void main(String[] args) {
        /* Check inputs and determine what the output file should be named. */
        if (args.length < 1) {
            System.out.println("Error: Input file argument required");
            System.out.println();
            System.exit(-4);
        }
        String fileName = args[0];
        File inputFile = new File(fileName);
        String output;
        ArrayList<String> files = buildFileList(inputFile);
        if (args.length > 1) {
            output = args[1]; 
        } else {
            output = getOutputFilename(inputFile);
        }
        System.out.println("Saving translation to: " + output);
        /* Create an instruction builder to parse the code and generate a list of
           VM instructions
         */
        InstructionBuilder builder = new InstructionBuilder(files.get(0));
        ArrayList<Instruction> instructions = new ArrayList<>();
        for (String currFile : files) {
            if (! currFile.equals(builder.fileName())) {
                builder.switchFile(currFile);
            }
            Instruction instr;
            do {
                instr = builder.buildNextInstruction();
                instructions.add(instr);
            }
            while (instr != null);
        };
        builder.close();
        /* Feed all the instructions into a CodeGenerator and append the output for each instruction
           to a StringBuilder
         */
        StringBuilder outputBuilder = new StringBuilder();
        CodeGenerator codeGen = new CodeGenerator(fileName);
        // TODO
        // outputBuilder.append(codeGen.generateInit());
        for (Instruction i : instructions) {
            if (i == null) {
                break;
            }
            String generated = codeGen.generateCode(i);
            outputBuilder.append(generated);
        }

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
