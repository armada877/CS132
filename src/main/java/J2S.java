import cs132.minijava.MiniJavaParser;
import cs132.minijava.ParseException;
import cs132.minijava.syntaxtree.Node;

import java.io.InputStream;

public class J2S {
    public static void main(String[] args) {
        try {
            InputStream in = System.in;
            Node root = new MiniJavaParser(in).Goal();
        } catch (ParseException e) {
            System.out.println(e.toString());
        }
    }
}
