package Typecheck;

import cs132.minijava.MiniJavaParser;
import cs132.minijava.ParseException;
import cs132.minijava.syntaxtree.Node;

import java.io.InputStream;

public class Typecheck {
    public static void main(String [] args){
        try {
            InputStream in = System.in;
            //InputStream in = new FileInputStream("testcases/hw2/basic.java");
            //InputStream in = new FileInputStream("testcases/hw2/TreeVisitor-error.java");
            Node root = new MiniJavaParser(in).Goal();
            MyVisitor visitor = new MyVisitor();
            SymbolVisitor symbolVisitor = new SymbolVisitor();
            CheckVisitor checkVisitor = new CheckVisitor();
            SymbolTable table = new SymbolTable();

            root.accept(symbolVisitor, table);
            root.accept(checkVisitor, table);
        } catch (ParseException e) {
            System.out.println(e.toString());
//        } catch (FileNotFoundException e) {
//            e.printStackTrace();
        }
        System.out.println("Program type checked successfully");
    }
}
