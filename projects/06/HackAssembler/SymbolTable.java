import java.util.HashMap;
import java.util.Set;

/** A SymbolTable holds the mappings between the following:
 *      - Labels -> Instruction Addresses
 *      - Symbols -> Data values
 *      - Jump, Dest, Comp mnemonics -> Binary Machine Code sequences
 *
 *      The label and symbol mappings are generated as an assembly code file is parsed, and the
 *      mnemonic mappings are read in from data files. A mapping is represented in the data file as
 *
 *      KEY VALUE
 *
 *      There must be at least one whitespace character between KEY and VALUE.
 *
 *      If a (key, value) mapping is created for a key that has an existing mapping in the table,
 *      the original value will be overwritten with the new value. Multiple keys can point to the same
 *      value.
 */

public class SymbolTable {
    /** The path to the directory where the mnemonic mappings are stored. */
    private static final String DATA_DIR = "./code_maps/";
    private static final int SYMBOL_ADDRESS_START = 16;
    private static final String PREDEFINED_SYMBOLS_FILE = "symbols.dat";
    private static final String DEST_CODES_FILE = "destCodes.dat";
    private static final String COMP_CODES_FILE = "compCodes.dat";
    private static final String JUMP_CODES_FILE = "jumpCodes.dat";
    private HashMap<String, String> table;
    private HashMap<String, String> destTable;
    private HashMap<String, String> compTable;
    private HashMap<String, String> jumpTable;

    /** The memory address of the next assembly code symbol to be added. These start at address 16. */
    int nextSymbolAddress;

    public SymbolTable() {
        table = new HashMap<>();
        destTable = new HashMap<>();
        compTable = new HashMap<>();
        jumpTable = new HashMap<>();
        buildTable(DATA_DIR + PREDEFINED_SYMBOLS_FILE, table);
        buildTable(DATA_DIR + DEST_CODES_FILE, destTable);
        buildTable(DATA_DIR + COMP_CODES_FILE, compTable);
        buildTable(DATA_DIR + JUMP_CODES_FILE, jumpTable);
        nextSymbolAddress = SYMBOL_ADDRESS_START;
    }

    /** Use a tokenizer to read in the mappings from FILENAME, these mappings
     *  are inserted into TABLE. Will fail if the data files are not structured
     *  correctly. Program will print an error message and exit. */
    private void buildTable(String fileName, HashMap<String, String> table) {
        Tokenizer t = new Tokenizer(fileName);
        Token symbol = null;
        Token value = null;
        while ((symbol = t.nextToken()) != null) {
            value = t.nextToken();
            if (value == null) {
                System.out.println("Invalid structure in " + fileName + ", Line " + symbol.lineNumber());
                System.exit(-8);
            }
            table.put(symbol.text(), value.text());
        }
        t.destroy();
    }

    /** Add a mapping to the symbol table. Repeated entries of a key will overwrite the value, but different keys
     *  can have the same value.
     */
    public void addSymbol(String symbol, String value) {
        table.put(symbol, value);
    }

    /** If SYMBOL is a key in the symbol table, returns the corresponding value. Otherwise, we assume that
     * SYMBOL is the name of a symbol in the Assembly code we are processing. This symbol is added to the symbol table
     * and given a memory address. Memory addresses start at 16 and increase by one for every symbol added. This
     * address is then returned.
     */
    public String getValue(String symbol) {
        String value = table.get(symbol);
        if (value == null) {
            table.put(symbol, Integer.toString(nextSymbolAddress));
            nextSymbolAddress++;
            return Integer.toString(nextSymbolAddress-1);
        }
        return value;
    }

    /** Look up the comp mnemonic and return the corresponding binary code sequence. */
    public String getCompCode(Token comp) {
        if (! compTable.containsKey(comp.text())) {
            throw new RuntimeException("Invalid comp field: " + comp.details());
        }
        return compTable.get(comp.text());
    }

    /** Look up the jump mnemonic and return the corresponding binary code sequence. */
    public String getJumpCode(Token jump) {
        if (! jumpTable.containsKey(jump.text())) {
            throw new RuntimeException("Invalid jump field: " + jump.details());
        }
        return jumpTable.get(jump.text());
    }

    /** Look up the dest mnemonic and return the corresponding binary code sequence. */
    public String getDestCode(Token dest) {
        if (! destTable.containsKey(dest.text())) {
            throw new RuntimeException("Invalid dest field: " + dest.details());
        }
        return destTable.get(dest.text());
    }
}
