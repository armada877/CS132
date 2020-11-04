import cs132.minijava.syntaxtree.*;
import cs132.minijava.visitor.GJDepthFirst;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class SymbolVisitor extends GJDepthFirst<String, SymbolTable> {

    @Override
    public String visit(MainClass n, SymbolTable argu) {
        n.f0.accept(this, argu);

        // Check id hasn't been used before
        TypeContainer classType = new TypeContainer(3, n.f1.f0.tokenImage);
        if (argu.classTable.contains(classType)) {
            // Cannot declare more than one class with the same name
            System.out.println("Type error");
            System.exit(1);
        }
        argu.classTable.add(classType);
        n.f1.accept(this, argu);

        n.f2.accept(this, argu);

        // Setup Global table
        argu.currentClass = n.f1.f0.tokenImage;
        argu.currentTable = new HashMap();

        // Add var declarations to field
        argu.addToFields(n);

        argu.allTables.put(argu.currentClass, new HashMap<>());
        argu.allTables.get(argu.currentClass).put("MEMBER-VAR-TABLE", argu.currentTable);

        // accept method declarations
        n.f15.accept(this, argu);

        argu.currentClass = null;

        return null;
    }

    /*
        f0 - "class"
        f1 - Identifier
        f2 - "{"
        f3 - NodeListOptional - Variable Declarations
        f4 - NodeListOptional - Method Declarations
        f5 - "}"
     */
    @Override
    public String visit(ClassDeclaration n, SymbolTable argu) {
        n.f0.accept(this, argu);

        // Check id hasn't been used before
        TypeContainer classType = new TypeContainer(3, n.f1.f0.tokenImage);
        if (argu.classTable.contains(classType)) {
            // Cannot declare more than one class with the same name
            System.out.println("Type error");
            System.exit(1);
        }
        argu.classTable.add(classType);
        n.f1.accept(this, argu);

        n.f2.accept(this, argu);

        // Setup Global table
        argu.currentClass = n.f1.f0.tokenImage;
        argu.currentTable = new HashMap();
        argu.linkset(n);

        // Add var declarations to field
        argu.addToFields(n);
        n.f3.accept(this, argu);

        argu.allTables.put(argu.currentClass, new HashMap<>());
        argu.allTables.get(argu.currentClass).put("MEMBER-VAR-TABLE", argu.currentTable);

        // accept method declarations
        n.f4.accept(this, argu);

        n.f5.accept(this, argu);

        argu.currentClass = null;

        return null;
    }

    @Override
    public String visit(ClassExtendsDeclaration n, SymbolTable argu) {
        n.f0.accept(this, argu);

        // Check id hasn't been used before
        TypeContainer classType = new TypeContainer(3, n.f1.f0.tokenImage);
        if (argu.classTable.contains(classType)) {
            // Cannot declare more than one class with the same name
            System.out.println("Type error");
            System.exit(1);
        }
        argu.classTable.add(classType);
        n.f1.accept(this, argu);

        n.f2.accept(this, argu);

        // parent class
        n.f3.accept(this, argu);

        n.f4.accept(this, argu);

        // Setup Global table
        argu.currentClass = n.f1.f0.tokenImage;
        argu.currentTable = new HashMap();
        argu.linkset(n);

        // Add var declarations to field
        argu.addToFields(n);
        n.f3.accept(this, argu);

        argu.allTables.put(argu.currentClass, new HashMap<>());
        argu.allTables.get(argu.currentClass).put("MEMBER-VAR-TABLE", argu.currentTable);

        // accept method declarations
        n.f4.accept(this, argu);

        n.f5.accept(this, argu);

        argu.currentClass = null;

        return null;
    }

    /*
        f0 - "public"
        f1 - Type
        f2 - Identifier
        f3 - (
        f4 - NodeOptonal - Formal Params
        f5 - )
        f6 - {
        f7 - NodeListOptional - Var Declarations
        f8 - NodeListOptional - Statements
        f9 - "return"
        f10 - Expression (for return)
        f11 - ";"
        f12 - }
     */
    @Override
    public String visit(MethodDeclaration n, SymbolTable argu) {
        n.f0.accept(this, argu);

        String typeDec = argu.getTypeName(n.f1);

        TypeContainer typeContainer = new TypeContainer(n.f1.f0.which, typeDec);
        n.f1.accept(this, argu);

        Pair<List<Pair<String, TypeContainer>>, TypeContainer> methodType =
                new Pair<List<Pair<String, TypeContainer>>, TypeContainer>(new ArrayList<>(), typeContainer);
        argu.methodTypes.put(new Pair<>(argu.currentClass, n.f2.f0.tokenImage), methodType);
        argu.currentMethod = n.f2.f0.tokenImage;
        n.f2.accept(this, argu);

        n.f3.accept(this, argu);
        argu.currentTable = new HashMap<>();

        // Accept formal params
        n.f4.accept(this, argu);

        n.f5.accept(this, argu);
        n.f6.accept(this, argu);

        // Var Declarations
        n.f7.accept(this, argu);
        if (argu.allTables.get(argu.currentClass).containsKey(argu.currentMethod)) {
            // Type Error, function overloaded
            System.out.println("Type error");
            System.exit(1);
        }
        argu.allTables.get(argu.currentClass).put(argu.currentMethod, argu.currentTable);

        // Statements (do nothing in symbol visitor)
        n.f8.accept(this, argu);

        // return statement (do nothing in symbol visitor)
        n.f9.accept(this, argu);
        n.f10.accept(this, argu);

        n.f11.accept(this, argu);
        n.f12.accept(this, argu);

        return null;
    }

    /*
        f0 - Type
        f1 - Identifier
        f2 - ;
     */
    @Override
    public String visit(VarDeclaration n, SymbolTable argu) {
        TypeContainer typeContainer = new TypeContainer(n.f0.f0.which, argu.getTypeName(n.f0));
        n.f0.accept(this, argu);
        argu.currentTable.put(n.f1.f0.tokenImage, typeContainer);
        n.f1.accept(this, argu);
        return null;
    }

    /*
        f0 - Type
        f1 - Identifier
     */
    @Override
    public String visit(FormalParameter n, SymbolTable argu) {
        // currentClass will be set by class declaration
        String typeDec = argu.getTypeName(n.f0);
        TypeContainer typeContainer = new TypeContainer(n.f0.f0.which, typeDec);
        n.f0.accept(this, argu);

        Pair<String, TypeContainer> paramType = new Pair(n.f1.f0.tokenImage, typeContainer);
        Pair<String, String> methodTypeID = new Pair(argu.currentClass, argu.currentMethod);

        argu.currentTable.put(n.f1.f0.tokenImage, typeContainer);
        argu.methodTypes.get(methodTypeID).fst.add(paramType);
        n.f1.accept(this, argu);

        return null;
    }
}
