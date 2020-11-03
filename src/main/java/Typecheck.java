import cs132.minijava.MiniJavaParser;
import cs132.minijava.ParseException;
import cs132.minijava.syntaxtree.Node;
import cs132.minijava.visitor.Visitor;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;

public class Typecheck {
    public static void main(String [] args){
        try {
            //InputStream in = System.in;
            InputStream in = new FileInputStream("testcases/hw2/basic.java");
            Node root = new MiniJavaParser(in).Goal();
            MyVisitor visitor = new MyVisitor();

            root.accept(visitor, null);
        } catch (ParseException | FileNotFoundException e) {
            System.out.println(e.toString());
        }
    }
}
