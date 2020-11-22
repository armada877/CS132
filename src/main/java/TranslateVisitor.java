import cs132.IR.sparrow.*;
import cs132.IR.sparrow.Block;
import cs132.IR.token.FunctionName;
import cs132.IR.token.Identifier;
import cs132.IR.token.Label;
import cs132.minijava.syntaxtree.*;
import cs132.minijava.visitor.GJNoArguDepthFirst;

import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;

public class TranslateVisitor extends GJNoArguDepthFirst<List> {
    private int k;
    public ArrayList<cs132.minijava.syntaxtree.Identifier> parameters;
    public ArrayList<cs132.minijava.syntaxtree.Identifier> localVars;
    public ArrayList<FunctionDecl> functionDecls;

    public MethodFieldTable methodFieldTable;

    public String currentClass;

    public TranslateVisitor(MethodFieldTable m) {
        super();
        k = 0;
        functionDecls = new ArrayList<>();
        methodFieldTable = m;
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

        List<Instruction> variables = n.f14.accept(this);
        List<Instruction> statements = n.f15.accept(this);
        if (!(variables == null))
            instructions.addAll(variables);
        if (!(statements == null))
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
    public List visit(ClassDeclaration n) {
        currentClass = n.f1.f0.tokenImage;

        n.f4.accept(this);

        return null;
    }

    @Override
    public List visit(MethodDeclaration n) {
        return super.visit(n);
    }

    @Override
    public List visit(AllocationExpression n) {
        int myK = k;
        Identifier objectPointer = new Identifier("w"+k);
        k += 1;
        String classType = n.f1.f0.tokenImage;
        ObjectTable objectTable = methodFieldTable.allObjects.get(classType);
        List<Instruction> instructions = new ArrayList<>();

        Identifier allocSize = new Identifier("w"+k);
        k += 1;

        int objectSize = 4 + (objectTable.fields.size() * 4); //Method table pointer + 4 * size of fields

        instructions.add(new Move_Id_Integer(allocSize, objectSize));
        instructions.add(new Alloc(objectPointer, allocSize));

        // Create method table
        for (String method : objectTable.methodTable) {

        }

        // Create fields

        //
    }

    @Override
    public List visit(AssignmentStatement n) {
        int myK = k;
        Identifier var = new Identifier(n.f0.f0.tokenImage);
        Expression expression = n.f2;
        List<Instruction> instructions = expression.accept(this);
        Instruction assignment = new Move_Id_Id(var, new Identifier("w"+myK));

        instructions.add(assignment);
        return instructions;
    }

    @Override
    public List visit(ArrayAssignmentStatement n) {
        int myK = k;

        Expression indexExpression = n.f2;
        Expression valueExpression = n.f5;

        List<Instruction> instructions = indexExpression.accept(this);
        int secondK = k;
        List<Instruction> valueInstructions = valueExpression.accept(this);

        Identifier array = new Identifier(n.f0.f0.tokenImage);
        Identifier arrIndex = new Identifier("w"+myK);
        Identifier value = new Identifier("w"+secondK);
        Identifier arrLength = new Identifier("w" + k);
        k += 1;
        Identifier arrPointer = new Identifier("w"+k);
        k += 1;
        Identifier arrOffset = new Identifier("w"+k);
        k += 1;
        Identifier fourConst = new Identifier("w"+k);
        k += 1;
        Identifier zeroConst = new Identifier("w"+k);
        k += 1;
        Identifier oneConst = new Identifier("w"+k);
        k += 1;
        Identifier lenCompare = new Identifier("w"+k);
        k += 1;
        Label endLabelNeg = new Label("end_l"+k);
        k += 1;
        Label errLabelOutOfBounds = new Label("error_l"+k);
        k += 1;
        Label endLabelOutOfBounds = new Label("end_l" + k);
        k += 1;
        Label nullLabel = new Label("null_l"+k);
        k += 1;
        Label endNullLabel = new Label("end_l" + k);
        k += 1;

        // declare constants
        instructions.add(new Move_Id_Integer(fourConst, 4));
        instructions.add(new Move_Id_Integer(oneConst, 1));
        instructions.add(new Move_Id_Integer(zeroConst, 0));

        /*
        arrLength = [array + 0]
        check index > 0
        check index < arrLength
        arrPointer = array + 4
         */
        // null check
        instructions.add(new IfGoto(array, nullLabel));
        instructions.add(new Goto(endNullLabel));
        instructions.add(new LabelInstr(nullLabel));
        instructions.add(new ErrorMessage("\"null pointer\""));
        instructions.add(new LabelInstr(endNullLabel));

        instructions.add(new Load(arrLength, array, 0));

        // negative Check
        instructions.add(new LessThan(lenCompare, arrIndex, zeroConst));
        instructions.add(new IfGoto(lenCompare, endLabelNeg));
        instructions.add(new ErrorMessage("\"array index out of bounds\""));
        instructions.add(new LabelInstr(endLabelNeg));

        // out of bounds check
        instructions.add(new LessThan(lenCompare, arrIndex, arrLength));
        instructions.add(new IfGoto(lenCompare, errLabelOutOfBounds));
        instructions.add(new Goto(endLabelOutOfBounds));
        instructions.add(new LabelInstr(errLabelOutOfBounds));
        instructions.add(new ErrorMessage("\"array index out of bounds\""));
        instructions.add(new LabelInstr(endLabelOutOfBounds));

        // Store value
        instructions.addAll(valueInstructions);
        instructions.add(new Add(arrOffset, arrIndex, oneConst));
        instructions.add(new Multiply(arrOffset, arrOffset, fourConst));
        instructions.add(new Add(arrPointer, array, arrOffset));
        instructions.add(new Store(arrPointer, 0, value));

        return instructions;

    }



    @Override
    public List visit(ArrayAllocationExpression n) {
        int myK = k;
        Identifier arrPointer = new Identifier("w"+myK);
        k += 1;

        Expression expression = n.f3;

        List<Instruction> instructions = expression.accept(this);
        Identifier arrayLength = new Identifier("w"+(myK+1));
        Identifier arraySize = new Identifier("w"+k);
        k += 1;
        Identifier fourConst = new Identifier("w"+k);
        k += 1;
        Identifier zeroConst = new Identifier("w"+k);
        k += 1;
        Identifier oneConst = new Identifier("w"+k);
        k += 1;
        Identifier lenCompare = new Identifier("w"+k);
        k += 1;
        Label endLabel = new Label("end_l"+k);
        k += 1;
        Identifier lenPlusOne = new Identifier("w"+k);
        k += 1;

        // if0 arrayLength < 0 goto end
        //      ErrorMessage ("a\"rray index out of bounds"\")
        // end
        //      Do array stuff
        instructions.add(new Move_Id_Integer(fourConst, 4));
        instructions.add(new Move_Id_Integer(oneConst, 1));

        // negative Check
        instructions.add(new Move_Id_Integer(zeroConst, 0));
        instructions.add(new LessThan(lenCompare, arrayLength, zeroConst));
        instructions.add(new IfGoto(lenCompare, endLabel));
        instructions.add(new ErrorMessage("\"array index out of bounds\""));
        instructions.add(new LabelInstr(endLabel));

        // Create Array
        instructions.add(new Add(lenPlusOne, oneConst, arrayLength));
        instructions.add(new Multiply(arraySize, lenPlusOne, fourConst));
        instructions.add(new Alloc(arrPointer, arraySize));
        instructions.add(new Store(arrPointer, 0, arrayLength));

        return instructions;
    }

    @Override
    public List visit(ArrayLookup n) {
        int myK = k;
        Identifier value = new Identifier("w"+k);
        k += 1;

        PrimaryExpression idExpression = n.f0;
        PrimaryExpression indexExpression = n.f2;
        List<Instruction> instructions = idExpression.accept(this);
        int secondK = k;
        List<Instruction> indexInstructions = indexExpression.accept(this);

        Identifier arrayId = new Identifier("w"+(myK+1));
        Identifier index = new Identifier("w"+secondK);

        Identifier arrLength = new Identifier("w" + k);
        k += 1;
        Identifier arrPointer = new Identifier("w"+k);
        k += 1;
        Identifier arrOffset = new Identifier("w"+k);
        k += 1;
        Identifier fourConst = new Identifier("w"+k);
        k += 1;
        Identifier zeroConst = new Identifier("w"+k);
        k += 1;
        Identifier oneConst = new Identifier("w"+k);
        k += 1;
        Identifier lenCompare = new Identifier("w"+k);
        k += 1;
        Label endLabelNeg = new Label("end_l"+k);
        k += 1;
        Label errLabelOutOfBounds = new Label("error_l"+k);
        k += 1;
        Label endLabelOutOfBounds = new Label("end_l" + k);
        k += 1;
        Label nullLabel = new Label("null_l"+k);
        k += 1;
        Label endNullLabel = new Label("endNullLabel_l"+k);
        k += 1;


        instructions.addAll(indexInstructions);

        // declare constants
        instructions.add(new Move_Id_Integer(fourConst, 4));
        instructions.add(new Move_Id_Integer(oneConst, 1));
        instructions.add(new Move_Id_Integer(zeroConst, 0));

        // null check
        instructions.add(new IfGoto(arrayId, nullLabel));
        instructions.add(new Goto(endNullLabel));
        instructions.add(new LabelInstr(nullLabel));
        instructions.add(new ErrorMessage("\"null pointer\""));
        instructions.add(new LabelInstr(endNullLabel));

        instructions.add(new Load(arrLength, arrayId, 0));

        // negative Check
        instructions.add(new LessThan(lenCompare, index, zeroConst));
        instructions.add(new IfGoto(lenCompare, endLabelNeg));
        instructions.add(new ErrorMessage("\"array index out of bounds\""));
        instructions.add(new LabelInstr(endLabelNeg));

        // out of bounds check
        instructions.add(new LessThan(lenCompare, index, arrLength));
        instructions.add(new IfGoto(lenCompare, errLabelOutOfBounds));
        instructions.add(new Goto(endLabelOutOfBounds));
        instructions.add(new LabelInstr(errLabelOutOfBounds));
        instructions.add(new ErrorMessage("\"array index out of bounds\""));
        instructions.add(new LabelInstr(endLabelOutOfBounds));

        // Load value
        instructions.add(new Add(arrOffset, index, oneConst));
        instructions.add(new Multiply(arrOffset, arrOffset, fourConst));
        instructions.add(new Add(arrPointer, arrayId, arrOffset));
        instructions.add(new Load(value, arrPointer, 0));

        return instructions;
    }

    @Override
    public List visit(ArrayLength n) {
        int myK = k;
        k += 1;
        PrimaryExpression expression = n.f0;
        List<Instruction> instructions = expression.accept(this);

        Identifier arrayLength = new Identifier("w"+myK);
        Identifier array = new Identifier("w" + (myK + 1));
        Label nullLabel = new Label("null_l"+k);
        k += 1;
        Label endNullLabel = new Label("endNull_l" + k);
        k += 1;

        // null check
        instructions.add(new IfGoto(array, nullLabel));
        instructions.add(new Goto(endNullLabel));
        instructions.add(new LabelInstr(nullLabel));
        instructions.add(new ErrorMessage("\"null pointer\""));
        instructions.add(new LabelInstr(endNullLabel));

        instructions.add(new Load(arrayLength, array, 0));

        return instructions;
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
    public List visit(VarDeclaration n) {
        String varName = n.f1.f0.tokenImage;
        localVars.add(n.f1);
        List<Instruction> instructions = new ArrayList<>();
        Identifier sparrowVar = new Identifier(varName);
        instructions.add(new Move_Id_Integer(sparrowVar, 0));
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

        Instruction subThemUp = new Subtract(sum, new Identifier("w"+(myK+1)), new Identifier("w"+secondK));

        firstInstructions.addAll(secondInstructions);
        firstInstructions.add(subThemUp);

        return firstInstructions;
    }

    @Override
    public List visit(TimesExpression n) {
        int myK = k;
        PrimaryExpression first = n.f0;
        PrimaryExpression second = n.f2;
        Identifier sum = new Identifier("w"+k);
        k += 1;

        List<Instruction> firstInstructions = first.accept(this);
        int secondK = k;
        List<Instruction> secondInstructions = second.accept(this);

        Instruction multThemUp = new Multiply(sum, new Identifier("w"+(myK+1)), new Identifier("w"+secondK));

        firstInstructions.addAll(secondInstructions);
        firstInstructions.add(multThemUp);

        return firstInstructions;
    }

    @Override
    public List visit(AndExpression n) {
        int myK = k;
        PrimaryExpression first = n.f0;
        PrimaryExpression second = n.f2;
        Identifier sum = new Identifier("w"+k);
        k += 1;

        List<Instruction> firstInstructions = first.accept(this);
        int secondK = k;
        List<Instruction> secondInstructions = second.accept(this);

        Instruction andThemUp = new Multiply(sum, new Identifier("w"+(myK+1)), new Identifier("w"+secondK));

        firstInstructions.addAll(secondInstructions);
        firstInstructions.add(andThemUp);

        return firstInstructions;
    }

    @Override
    public List visit(CompareExpression n) {
        int myK = k;
        PrimaryExpression first = n.f0;
        PrimaryExpression second = n.f2;
        Identifier sum = new Identifier("w"+k);
        k += 1;

        List<Instruction> firstInstructions = first.accept(this);
        int secondK = k;
        List<Instruction> secondInstructions = second.accept(this);

        Instruction compThemUp = new LessThan(sum, new Identifier("w"+(myK+1)), new Identifier("w"+secondK));

        firstInstructions.addAll(secondInstructions);
        firstInstructions.add(compThemUp);

        return firstInstructions;
    }

    @Override
    public List visit(NotExpression n) {
        int myK = k;
        Identifier notValue = new Identifier("w"+k);
        k += 1;

        List<Instruction> instructions = n.f1.accept(this);
        Identifier expressionValue = new Identifier("w"+ (myK+1));

        Identifier oneValue = new Identifier("w"+k);
        k += 1;

        instructions.add(new Move_Id_Integer(oneValue, 1));

        instructions.add(new Subtract(notValue, oneValue, expressionValue));
        return instructions;
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
    public List visit(TrueLiteral n) {
        boolean booleanValue = (new Boolean(n.f0.tokenImage)).booleanValue();
        Identifier identifier = new Identifier("w"+k);
        k += 1;
        Instruction initializeLiteral = new Move_Id_Integer(identifier, 1);

        List instructionList = new ArrayList();
        instructionList.add(initializeLiteral);

        return instructionList;
    }

    @Override
    public List visit(FalseLiteral n) {
        boolean booleanValue = (new Boolean(n.f0.tokenImage)).booleanValue();
        Identifier identifier = new Identifier("w"+k);
        k += 1;
        Instruction initializeLiteral = new Move_Id_Integer(identifier, 0);

        List instructionList = new ArrayList();
        instructionList.add(initializeLiteral);

        return instructionList;
    }

    @Override
    public List visit(IfStatement n) {
        int myK = k;
        Expression expression = n.f2;

        List<Instruction> instructions = expression.accept(this);
        Identifier conditional = new Identifier("w"+myK);
        Label condFalse = new Label("l_else"+myK);
        k += 1;
        Label end = new Label("l_end"+myK);
        k += 1;

        instructions.add(new IfGoto(conditional, condFalse)); // if0 statement
        instructions.addAll(n.f4.accept(this));
        instructions.add(new Goto(end));
        instructions.add(new LabelInstr(condFalse));
        instructions.addAll(n.f6.accept(this));
        instructions.add(new Goto(end));
        instructions.add(new LabelInstr(end));

        return instructions;
    }

    @Override
    public List visit(WhileStatement n) {
        Expression expression = n.f2;

        List<Instruction> instructions = new ArrayList<>();
        Label start = new Label("l_start"+k);
        k += 1;
        instructions.add(new LabelInstr(start));

        int myK = k;

        List<Instruction> exprInstructions = expression.accept(this);
        Identifier conditional = new Identifier("w"+myK);

        Label condFalse = new Label("l_end"+k);

        instructions.addAll(exprInstructions);
        instructions.add(new IfGoto(conditional, condFalse));
        instructions.addAll(n.f4.accept(this));
        instructions.add(new Goto(start));
        instructions.add(new LabelInstr(condFalse));

        return instructions;
    }

    @Override
    public List visit(cs132.minijava.syntaxtree.Identifier n) {
        String varName = n.f0.tokenImage;
        List instructionList = new ArrayList();

        if (localVars.contains(n)) {
            Identifier identifier = new Identifier("w" + k);
            k += 1;
            Instruction initializeVar = new Move_Id_Id(identifier, new Identifier(varName));
            instructionList.add(initializeVar);
        } else if (parameters.contains(n)) {
            // it's a parameter
        } else {
            // it's a field
        }
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
    public List visit(cs132.minijava.syntaxtree.Block n) {
        return n.f1.accept(this);
    }

    @Override
    public List visit(BracketExpression n) {
        return n.f1.accept(this);
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
