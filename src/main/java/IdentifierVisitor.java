import Typecheck.Pair;
import Typecheck.TypeContainer;
import cs132.minijava.syntaxtree.*;
import cs132.minijava.visitor.GJDepthFirst;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class IdentifierVisitor extends GJDepthFirst<String, SymbolGenerator> {

    @Override
    public String visit(MainClass n, SymbolGenerator argu) {
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
        // argu.allTables.get(argu.currentClass).put("MEMBER-VAR-TABLE", argu.currentTable);

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
    public String visit(ClassDeclaration n, SymbolGenerator argu) {
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
    public String visit(ClassExtendsDeclaration n, SymbolGenerator argu) {
        n.f0.accept(this, argu);

        // Check id hasn't been used before
        TypeContainer classType = new TypeContainer(3, n.f1.f0.tokenImage);
        if (argu.classTable.contains(classType)) {
            // Cannot declare more than one class with the same name
            System.out.println("Type error");
            System.exit(0);
        }
        argu.classTable.add(classType);
        n.f1.accept(this, argu);

        n.f2.accept(this, argu);

        argu.currentClass = n.f1.f0.tokenImage;
        argu.currentTable = new HashMap();

        // parent class
        n.f3.accept(this, argu);
        argu.linksets_map.put(argu.currentClass, n.f3.f0.tokenImage);
        argu.parent = n.f3.f0.tokenImage;

        n.f4.accept(this, argu);

        // Setup Global table
        argu.linkset(n);

        // Add var declarations to field
        argu.addToFields(n);
        n.f3.accept(this, argu);

        argu.allTables.put(argu.currentClass, new HashMap<>());
        argu.allTables.get(argu.currentClass).put("MEMBER-VAR-TABLE", argu.currentTable);

        // accept method declarations
        n.f4.accept(this, argu);

        n.f5.accept(this, argu);
        n.f6.accept(this, argu);

        // add all methods from parent classes not declared and check for acyclicity
        String currentParent = argu.parent;
        for (Pair<String, String> methodDec : argu.methodTypes.keySet()) {
            if (methodDec.fst == currentParent) {
                if (!argu.methodTypes.containsKey(new Pair<>(argu.currentClass, methodDec.snd))) {
                    argu.methodTypes.put(new Pair<>(argu.currentClass, methodDec.snd), argu.methodTypes.get(methodDec));
                }
            }
        }
        while (currentParent != "") {
            if (argu.linksets_map.containsKey(currentParent)) {
                currentParent = argu.linksets_map.get(currentParent);
            } else {
                currentParent = "";
            }
            // acyclicity check
            if (currentParent == argu.currentClass) {
                System.out.println("Type error");
                System.exit(0);
            }
        }

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
    public String visit(MethodDeclaration n, SymbolGenerator argu) {
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
            System.exit(0);
        }
        argu.allTables.get(argu.currentClass).put(argu.currentMethod, argu.currentTable);

        if (argu.linksets_map.containsKey(argu.currentClass)) {
            String parentClass = argu.linksets_map.get(argu.currentClass);
            Pair<String, String> parentMethod = new Pair<>(n.f2.f0.tokenImage, parentClass);
            if (argu.methodTypes.containsKey(parentMethod)) { // method exists in parent
                // check return type matches
                if(!argu.methodTypes.get(parentMethod).snd.equals(methodType.snd)) {
                    System.out.println("Type error");
                    System.exit(0);
                }
                // check params match
                int count = 0;
                for (Pair<String, TypeContainer> param : argu.methodTypes.get(parentMethod).fst) {
                    Pair<String, TypeContainer> current_param = methodType.fst.get(count);
                    // parameter name
                    if (current_param.fst != param.fst) {
                        System.out.println("Type error");
                        System.exit(0);
                    }

                    // parameter type
                    if (!current_param.snd.equals(param.snd)) {
                        System.out.println("Type error");
                        System.exit(0);
                    }
                }

                // Add all non-declared parent methods to methodTypes
            }

        }

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
    public String visit(VarDeclaration n, SymbolGenerator argu) {
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
    public String visit(FormalParameter n, SymbolGenerator argu) {
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
