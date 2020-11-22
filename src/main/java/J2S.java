import cs132.IR.sparrow.Program;
import cs132.minijava.MiniJavaParser;
import cs132.minijava.ParseException;
import cs132.minijava.syntaxtree.Node;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;

public class J2S {
    public static void main(String[] args) {
        try {
            InputStream in = System.in;
//            InputStream in = new FileInputStream("testcases/hw3/QuickSort.java");
            new MiniJavaParser(in);
            Node root = MiniJavaParser.Goal();

            MethodFieldTable methodFieldTable = new MethodFieldTable();
            MethodFieldTableVisitor methodFieldTableVisitor = new MethodFieldTableVisitor(methodFieldTable);
            root.accept(methodFieldTableVisitor);

            methodFieldTable.generateTree();
            methodFieldTable.generateAllObjects(methodFieldTable.root);

            TranslateVisitor translateVisitor = new TranslateVisitor(methodFieldTable);
            root.accept(translateVisitor);
            Program program = new Program(translateVisitor.functionDecls);
            System.out.println(program.toString());
        } catch (ParseException e) {
            System.out.println(e.toString());
//        } catch (FileNotFoundException e) {
//            System.out.println(e.toString());
        }
    }
}
