import cs132.IR.sparrow.*;
import cs132.IR.sparrow.Block;
import cs132.IR.token.FunctionName;
import cs132.IR.token.Identifier;
import cs132.minijava.syntaxtree.*;
import cs132.minijava.visitor.GJNoArguDepthFirst;

import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;

public class TranslateVisitor extends GJNoArguDepthFirst<List> {
    private int k;
    public ArrayList<FunctionDecl> functionDecls;

    public TranslateVisitor() {
        super();
        k = 0;
        functionDecls = new ArrayList<>();
    }

    @Override
    public List visit(MainClass n) {
        k = 0;
        FunctionName funcName = new FunctionName("Main");
        List<Identifier> params = new ArrayList<>();
        List<Instruction> instructions = new ArrayList<>();
        Identifier returnId = new Identifier("thisisthereturnvar");

        Instruction initializeRetVal = new Move_Id_Integer(returnId, 0);
        instructions.add(initializeRetVal);

        // TODO: Visit java variable decl and translate
        List<Instruction> variables = n.f14.accept(this);
        List<Instruction> statements = n.f15.accept(this);
        instructions.addAll(statements);

        Block block = new Block(instructions, returnId);

        for (Instruction i : block.instructions) {
            i.parent = block;
        }

        FunctionDecl mainFunc = new FunctionDecl(funcName, params, block);
        block.parent = mainFunc;

        functionDecls.add(mainFunc);
        return null;
    }


    @Override
    public List visit(PrintStatement n) {
        int myK = k;
        Expression expression = n.f2;
        Identifier printVal = new Identifier("w"+k);
        k += 1;
        List<Instruction> instructions = expression.accept(this);
        Instruction assignInstruction = new Move_Id_Id(printVal, new Identifier("w" + (myK + 1)));
        Instruction printInstruction = new Print(printVal);

        instructions.add(assignInstruction);
        instructions.add(printInstruction);

        return instructions;
    }

    @Override
    public List visit(PlusExpression n) {
        int myK = k;
        PrimaryExpression first = n.f0;
        PrimaryExpression second = n.f2;
        Identifier sum = new Identifier("w"+k);
        k += 1;

        List<Instruction> firstInstructions = first.accept(this);
        int secondK = k;
        List<Instruction> secondInstructions = second.accept(this);

        Instruction addThemUp = new Add(sum, new Identifier("w"+(myK+1)), new Identifier("w"+secondK));

        firstInstructions.addAll(secondInstructions);
        firstInstructions.add(addThemUp);

        return firstInstructions;
    }

    @Override
    public List visit(MinusExpression n) {
        int myK = k;
        PrimaryExpression first = n.f0;
        PrimaryExpression second = n.f2;
        Identifier sum = new Identifier("w"+k);
        k += 1;

        List<Instruction> firstInstructions = first.accept(this);
        int secondK = k;
        List<Instruction> secondInstructions = second.accept(this);

        Instruction addThemUp = new Subtract(sum, new Identifier("w"+(myK+1)), new Identifier("w"+secondK));

        firstInstructions.addAll(secondInstructions);
        firstInstructions.add(addThemUp);

        return firstInstructions;
    }

    @Override
    public List visit(IntegerLiteral n) {
        int integer = (new Integer(n.f0.tokenImage)).intValue();
        Identifier identifier = new Identifier("w"+k);
        k += 1;
        Instruction initializeLiteral = new Move_Id_Integer(identifier, integer);

        List instructionList = new ArrayList();
        instructionList.add(initializeLiteral);

        return instructionList;
    }

    @Override
    public List visit(Expression n) {
        return n.f0.accept(this);
    }

    @Override
    public List visit(PrimaryExpression n) {
        return n.f0.accept(this);
    }

    @Override
    public List visit(Statement n) {
        return n.f0.accept(this);
    }

    @Override
    public List visit(NodeListOptional n) {
        if (!n.present()) {
            return null;
        } else {
            List _ret = new ArrayList();
            int _count = 0;

            for(Enumeration e = n.elements(); e.hasMoreElements(); ++_count) {
                _ret.addAll(((Node)e.nextElement()).accept(this));
            }

            return _ret;
        }
    }
}
