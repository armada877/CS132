import cs132.IR.ParseException;
import cs132.IR.SparrowParser;
import cs132.IR.registers.Registers;
import cs132.IR.sparrowv.Program;
import cs132.IR.syntaxtree.Node;
import cs132.IR.visitor.SparrowVConstructor;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;

public class SV2V {
    public static void main(String[] args) {
        Registers.SetRiscVregs();
        try {
//            InputStream in = System.in;
            InputStream in = new FileInputStream("testcases/hw5/factorial5.sparrow-v");
            SparrowParser sparrowParser = new SparrowParser(in);
            Node root = sparrowParser.Program();
            SparrowVConstructor sparrowConstructor = new SparrowVConstructor();
            root.accept(sparrowConstructor);
            Program program = sparrowConstructor.getProgram();
            ContextVisitor contextVisitor = new ContextVisitor();
            program.accept(contextVisitor);
            System.out.println("oof");
        } catch (ParseException e) {
            System.out.println(e.toString());
        } catch (FileNotFoundException e) {
            System.out.println(e.toString());
        }
    }
}
