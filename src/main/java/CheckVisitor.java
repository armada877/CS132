import cs132.minijava.syntaxtree.*;
import cs132.minijava.visitor.GJDepthFirst;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class CheckVisitor extends GJDepthFirst<TypeContainer, SymbolTable> {
    @Override
    public TypeContainer visit(MainClass n, SymbolTable argu) {
        n.f0.accept(this, argu);

        // Check id hasn't been used before
        TypeContainer classType = new TypeContainer(3, n.f1.f0.tokenImage);

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
    public TypeContainer visit(ClassDeclaration n, SymbolTable argu) {
        n.f0.accept(this, argu);

        // set current class to choose scope
        argu.currentClass = n.f1.f0.tokenImage;

        n.f2.accept(this, argu);

        // var declarations already processed
        n.f3.accept(this, argu);

        // accept method declarations
        n.f4.accept(this, argu);

        n.f5.accept(this, argu);

        argu.currentClass = null;

        return null;
    }

    @Override
    public TypeContainer visit(ClassExtendsDeclaration n, SymbolTable argu) {
        n.f0.accept(this, argu);

        // set current class to choose scope
        argu.currentClass = n.f1.f0.tokenImage;

        n.f6.accept(this, argu);
        argu.currentClass = null;
        return null;
    }

    /*
        f0 - "public"
        f1 - Type
        f2 - Identifier
        f3 - (
        f4 - NodeOptional - Formal Params
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
    public TypeContainer visit(MethodDeclaration n, SymbolTable argu) {
        n.f0.accept(this, argu);

        // set current method for scope
        argu.currentMethod = n.f2.f0.tokenImage;
        argu.currentTable = argu.allTables.get(argu.currentClass).get(argu.currentMethod);

        n.f3.accept(this, argu);

        n.f5.accept(this, argu);
        n.f6.accept(this, argu);

        n.f7.accept(this, argu);

        // Statements
        n.f8.accept(this, argu);

        // return statement (do nothing in symbol visitor)
        n.f9.accept(this, argu);

        TypeContainer tc = n.f10.accept(this, argu);
        Pair<String, String> class_meth = new Pair<>(argu.currentClass, argu.currentMethod);
        if (!tc.equals(argu.methodTypes.get(class_meth).snd)){
            System.out.println("Type error");
            System.exit(0);
        }

        n.f11.accept(this, argu);
        n.f12.accept(this, argu);

        return null;
    }

    @Override
    public TypeContainer visit(VarDeclaration n, SymbolTable argu) {
        return null;
    }

    /*
            f0 - Identifier
            f1 - =
            f2 - expression
            f3 - ;
         */
    @Override
    public TypeContainer visit(AssignmentStatement n, SymbolTable argu) {
        TypeContainer typeContainer = null;
        String targetVar = n.f0.f0.tokenImage;
        if (argu.currentTable.containsKey(targetVar)) {
            typeContainer = argu.currentTable.get(targetVar);
        } else if (argu.fields.get(argu.currentClass).containsKey(targetVar)) {
            typeContainer = argu.fields.get(argu.currentClass).get(targetVar);
        } else {

            System.out.println("Type error");
            System.exit(0);
        }
        n.f0.accept(this, argu);
        n.f1.accept(this, argu);
        TypeContainer exprType = n.f2.accept(this, argu);

        if (!exprType.equals(typeContainer)) {
            System.out.println("Type error");
            System.exit(0);
        }


        return super.visit(n, argu);
    }

    /*
        f0 - nodeChoice
     */
    @Override
    public TypeContainer visit(Expression n, SymbolTable argu) {
        return n.f0.accept(this, argu);
    }

    @Override
    public TypeContainer visit(ArrayAssignmentStatement n, SymbolTable argu) {
        TypeContainer tc = null;

        tc = n.f0.accept(this, argu);
        if (tc.type != TypeContainer.Type.INTARR) {
            System.out.println("Type error");
            System.exit(0);
        }

        tc = n.f2.accept(this, argu);
        if (tc.type != TypeContainer.Type.INT) {
            System.out.println("Type error");
            System.exit(0);
        }

        tc = n.f5.accept(this, argu);
        if (tc.type != TypeContainer.Type.INT) {
            System.out.println("Type error");
            System.exit(0);
        }

        return null;
    }

    @Override
    public TypeContainer visit(ArrayAllocationExpression n, SymbolTable argu) {
        n.f0.accept(this, argu);
        n.f1.accept(this, argu);
        n.f2.accept(this, argu);
        TypeContainer type = n.f3.accept(this, argu);

        if (type.type != TypeContainer.Type.INT) {
            System.out.println("Type error");
            System.exit(0);
        }

        n.f4.accept(this, argu);

        return new TypeContainer(0, "");
    }

    @Override
    public TypeContainer visit(PrimaryExpression n, SymbolTable argu) {
        return n.f0.accept(this, argu);
    }

    @Override
    public TypeContainer visit(TrueLiteral n, SymbolTable argu) {
        n.f0.accept(this, argu);
        return new TypeContainer(1, "");
    }

    @Override
    public TypeContainer visit(FalseLiteral n, SymbolTable argu) {
        n.f0.accept(this, argu);
        return new TypeContainer(1, "");
    }

    @Override
    public TypeContainer visit(IntegerLiteral n, SymbolTable argu) {
        n.f0.accept(this, argu);
        return new TypeContainer(2, "");
    }

    @Override
    public TypeContainer visit(Identifier n, SymbolTable argu) {
        n.f0.accept(this, argu);

        if (!argu.currentTable.containsKey(n.f0.tokenImage) && !argu.fields.get(argu.currentClass).containsKey(n.f0.tokenImage)) {
            System.out.println("Type error");
            System.exit(0);
        }

        TypeContainer tc = argu.currentTable.get(n.f0.tokenImage);
        if (tc == null) {
            tc = argu.fields.get(argu.currentClass).get(n.f0.tokenImage);
        }
        return tc;
    }

    @Override
    public TypeContainer visit(AllocationExpression n, SymbolTable argu) {
        String objType = n.f1.f0.tokenImage;
        TypeContainer objectTypeContainer = new TypeContainer(3, objType);
        if (!argu.classTable.contains(objectTypeContainer)){
            System.out.println("Type error");
            System.exit(0);
        }
        return objectTypeContainer;
    }

    @Override
    public TypeContainer visit(ThisExpression n, SymbolTable argu) {
        return new TypeContainer(3, argu.currentClass);
    }

    @Override
    public TypeContainer visit(PlusExpression n, SymbolTable argu) {
        TypeContainer a = n.f0.accept(this, argu);
        TypeContainer b = n.f2.accept(this, argu);
        if (a.type != TypeContainer.Type.INT || b.type != TypeContainer.Type.INT) {
            System.out.println("Type error");
            System.exit(0);
        }
        return a;
    }

    @Override
    public TypeContainer visit(MinusExpression n, SymbolTable argu) {
        TypeContainer a = n.f0.accept(this, argu);
        TypeContainer b = n.f2.accept(this, argu);
        if (a.type != TypeContainer.Type.INT || b.type != TypeContainer.Type.INT) {
            System.out.println("Type error");
            System.exit(0);
        }
        return a;
    }

    @Override
    public TypeContainer visit(TimesExpression n, SymbolTable argu) {
        TypeContainer a = n.f0.accept(this, argu);
        TypeContainer b = n.f2.accept(this, argu);
        if (a.type != TypeContainer.Type.INT || b.type != TypeContainer.Type.INT) {
            System.out.println("Type error");
            System.exit(0);
        }
        return a;
    }

    @Override
    public TypeContainer visit(ArrayLookup n, SymbolTable argu) {
        TypeContainer a = n.f0.accept(this, argu);
        TypeContainer b = n.f2.accept(this, argu);
        if (a.type != TypeContainer.Type.INTARR || b.type != TypeContainer.Type.INT) {
            System.out.println("Type error");
            System.exit(0);
        }
        return b;
    }


    @Override
    public TypeContainer visit(ArrayLength n, SymbolTable argu) {
        TypeContainer a = n.f0.accept(this, argu);
        if (a.type != TypeContainer.Type.INTARR) {
            System.out.println("Type error");
            System.exit(0);
        }
        return new TypeContainer(2, "");
    }

    @Override
    public TypeContainer visit(CompareExpression n, SymbolTable argu) {
        TypeContainer a = n.f0.accept(this, argu);
        TypeContainer b = n.f2.accept(this, argu);
        if (a.type != TypeContainer.Type.INT || b.type != TypeContainer.Type.INT) {
            System.out.println("Type error");
            System.exit(0);
        }
        return new TypeContainer(1, "");
    }

    @Override
    public TypeContainer visit(AndExpression n, SymbolTable argu) {
        TypeContainer a = n.f0.accept(this, argu);
        TypeContainer b = n.f2.accept(this, argu);
        if (a.type != TypeContainer.Type.BOOL || b.type != TypeContainer.Type.BOOL) {
            System.out.println("Type error");
            System.exit(0);
        }
        return a;
    }

    @Override
    public TypeContainer visit(NotExpression n, SymbolTable argu) {
        TypeContainer a = n.f1.accept(this, argu);
        if (a.type != TypeContainer.Type.BOOL) {
            System.out.println("Type error");
            System.exit(0);
        }
        return a;
    }

    @Override
    public TypeContainer visit(BracketExpression n, SymbolTable argu) {
        return n.f1.accept(this, argu);
    }

    @Override
    public TypeContainer visit(MessageSend n, SymbolTable argu) {
        TypeContainer caller = n.f0.accept(this, argu);
        String method = n.f2.f0.tokenImage;
        Pair<String, String> caller_method = new Pair<>(caller.typeName, method);

        // Class exists
        if (!argu.classTable.contains(caller)) {
            System.out.println("Type error");
            System.exit(0);
        }
        if (!argu.methodTypes.containsKey(caller_method)) {
            System.out.println("Type error");
            System.exit(0);
        }

        List<Pair<String, TypeContainer>> params = argu.methodTypes.get(caller_method).fst;
        TypeContainer returnType = argu.methodTypes.get(new Pair<>(caller.typeName, method)).snd;

        if (n.f4.node instanceof  ExpressionList) {
            ExpressionList el = (ExpressionList) n.f4.node;
            int numParams = 1 + el.f1.nodes.size();
            if (numParams != params.size()) {
                System.out.println("Type error");
                System.exit(0);
            }
            TypeContainer tc = el.f0.accept(this, argu);
            if (!tc.equals(params.get(0).snd)) {
                // check for subtype
                boolean subtype = false;
                if (params.get(0).snd.type == TypeContainer.Type.OBJ && argu.linksets_map.containsKey(tc.typeName)) {
                    String parent = argu.linksets_map.get(tc.typeName);
                    while (parent != "") {
                        if (parent == params.get(0).snd.typeName) {
                            // we're good
                            subtype = true;
                            break;
                        } else {
                            parent = argu.linksets_map.get(parent);
                        }
                    }
                }
                if (!subtype) {
                    System.out.println("Type error");
                    System.exit(0);
                }
            }
            int counter = 1;
            for (Node node : el.f1.nodes) {
                tc = node.accept(this, argu);
                if (!tc.equals(params.get(counter).snd)) {
                    // check for subtype
                    boolean subtype = false;
                    if (params.get(counter).snd.type == TypeContainer.Type.OBJ && argu.linksets_map.containsKey(tc.typeName)) {
                        String parent = argu.linksets_map.get(tc.typeName);
                        while (parent != "") {
                            if (parent == params.get(counter).snd.typeName) {
                                // we're good
                                subtype = true;
                                break;
                            } else {
                                parent = argu.linksets_map.get(parent);
                            }
                        }
                    }
                    if (!subtype) {
                        System.out.println("Type error");
                        System.exit(0);
                    }
                }
                counter += 1;
            }
        }

        return returnType;
    }

    @Override
    public TypeContainer visit(ExpressionRest n, SymbolTable argu) {
        return n.f1.accept(this, argu);
    }

    @Override
    public TypeContainer visit(PrintStatement n, SymbolTable argu) {
        TypeContainer tc = new TypeContainer(2, "");
        if (!n.f2.accept(this, argu).equals(tc)) {
            System.out.println("Type error");
            System.exit(0);
        }
        return null;
    }

    @Override
    public TypeContainer visit(WhileStatement n, SymbolTable argu) {
        TypeContainer conditional = n.f2.accept(this, argu);
        if (!conditional.type.equals(TypeContainer.Type.BOOL)) {
            System.out.println("Type error");
            System.exit(0);
        }
        n.f4.accept(this, argu);
        return null;
    }

    @Override
    public TypeContainer visit(IfStatement n, SymbolTable argu) {
        TypeContainer conditional = n.f2.accept(this, argu);
        if (!conditional.type.equals(TypeContainer.Type.BOOL)) {
            System.out.println("Type error");
            System.exit(0);
        }
        n.f4.accept(this, argu);
        n.f6.accept(this, argu);
        return null;
    }
}
