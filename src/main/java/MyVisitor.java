
import cs132.minijava.syntaxtree.*;
import cs132.minijava.visitor.GJDepthFirst;

import java.util.*;

public class MyVisitor extends GJDepthFirst {
    HashMap<String, String> table;

    public MyVisitor() {
        table = new HashMap<>();
    }



    @Override
    public Object visit(ClassDeclaration n, Object argu) {
        System.out.println("Visited a class declaration");
        return super.visit(n, argu);
    }

    @Override
    public Object visit(VarDeclaration n, Object argu) {
        String type = n.f0.f0.toString();
        String var = n.f1.f0.tokenImage;
        return super.visit(n, argu);
    }

    @Override
    public Object visit(AssignmentStatement n, Object argu) {
        String var = n.f0.f0.tokenImage;

        if (table.get(var) == null){
            System.out.println("Type error");
            System.exit(1);
        }
        System.out.println("Visited an assignment Statement");
        return super.visit(n, argu);
    }
}
