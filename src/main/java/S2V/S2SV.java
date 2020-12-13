package S2V;

import cs132.IR.ParseException;
import cs132.IR.SparrowParser;
import cs132.IR.registers.Registers;
import cs132.IR.sparrow.Program;
import cs132.IR.syntaxtree.Node;
import cs132.IR.visitor.SparrowConstructor;

import java.io.InputStream;

public class S2SV {
    public static void main(String[] args) {
        Registers.SetRiscVregs();
        try {
            InputStream in = System.in;
//            InputStream in = new FileInputStream("testcases/hw4/strech.sparrow");
            SparrowParser sparrowParser = new SparrowParser(in);
            Node root = sparrowParser.Program();
            SparrowConstructor sparrowConstructor = new SparrowConstructor();
            root.accept(sparrowConstructor);
            Program program = sparrowConstructor.getProgram();
            LivenessVisitor livenessVisitor = new LivenessVisitor();
            program.accept(livenessVisitor);
            LinearScanRegisterAllocator linearScanRegisterAllocator = new LinearScanRegisterAllocator(livenessVisitor.liveness);
            TranslateVisitor translateVisitor = new TranslateVisitor(linearScanRegisterAllocator, livenessVisitor);
            program.accept(translateVisitor);
            cs132.IR.sparrowv.Program program1 = new cs132.IR.sparrowv.Program(translateVisitor.functionDecls);
            System.out.println(program1.toString());
        } catch (ParseException e) {
            System.out.println(e.toString());
//        } catch (FileNotFoundException e) {
//            System.out.println(e.toString());
        }
    }
}
