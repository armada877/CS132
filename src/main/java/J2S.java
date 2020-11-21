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
//            InputStream in = System.in;
            InputStream in = new FileInputStream("testcases/hw3/1-PrintLiteral.java");
            new MiniJavaParser(in);
            Node root = MiniJavaParser.Goal();

            //SymbolGenerator symbolGenerator = new SymbolGenerator();
            //IdentifierVisitor identifierVisitor = new IdentifierVisitor();
            TranslateVisitor translateVisitor = new TranslateVisitor();
            root.accept(translateVisitor);
            Program program = new Program(translateVisitor.functionDecls);
            System.out.println(program.toString());
        } catch (ParseException e) {
            System.out.println(e.toString());
        } catch (FileNotFoundException e) {
            System.out.println(e.toString());
        }
    }
}
