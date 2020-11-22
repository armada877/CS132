import Typecheck.Pair;
import Typecheck.TypeContainer;
import cs132.minijava.syntaxtree.*;
import cs132.minijava.visitor.DepthFirstVisitor;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class MethodFieldTableVisitor extends DepthFirstVisitor {
    ObjectTable currentObject;
    MethodFieldTable methodFieldTable;

    public MethodFieldTableVisitor(MethodFieldTable m) {
        methodFieldTable = m;
    }

    @Override
    public void visit(ClassDeclaration n) {
        currentObject = new ObjectTable();
        // Setup object table
        currentObject.parent = methodFieldTable.root;
        methodFieldTable.root.children.add(currentObject);
        currentObject.objectName = n.f1.f0.tokenImage;

        // Accept Fields
        n.f3.accept(this);

        // Accept Method Declarations
        n.f4.accept(this);
    }

    @Override
    public void visit(ClassExtendsDeclaration n) {
        currentObject = new ObjectTable();
        // Setup Object table
        currentObject.objectName = n.f1.f0.tokenImage;
        methodFieldTable.childToParentMap.put(n.f1.f0.tokenImage, n.f3.f0.tokenImage);

        methodFieldTable.orphans.add(currentObject);

        // Accept Fields
        n.f5.accept(this);


        // Accept Method Declarations
        n.f6.accept(this);
    }

    @Override
    public void visit(VarDeclaration n) {
        // Add variable to fields
        currentObject.localFields.add(n.f1.f0.tokenImage);
    }

    @Override
    public void visit(MethodDeclaration n) {
        // add method declaration to method table
        currentObject.localMethods.add(n.f2.f0.tokenImage);
    }
}
