import cs132.IR.sparrowv.*;
import cs132.IR.sparrowv.visitor.Visitor;
import cs132.IR.token.Identifier;

import java.util.*;

public class ContextVisitor implements Visitor {
    public Map<String, Map<String, Integer>> stackRef;
    private String currentFunc;
    private Map<String, Integer> cStackRef;
    private int sp;

    public ContextVisitor() {
        stackRef = new HashMap<>();
    }

    @Override
    public void visit(Program program) {
        for (FunctionDecl functionDecl : program.funDecls) {
            functionDecl.accept(this);
        }
    }

    @Override
    public void visit(FunctionDecl functionDecl) {
        currentFunc = functionDecl.functionName.name;
        stackRef.put(currentFunc, new HashMap<>());
        cStackRef = stackRef.get(currentFunc);
        sp = -12;

        List<Identifier> params = new ArrayList(functionDecl.formalParameters);
        int c = 0;

        for (Identifier param : params) {
            cStackRef.put(param.toString(), c);
            c += 4;
        }

        functionDecl.block.accept(this);
    }

    @Override
    public void visit(Block block) {
        for (Instruction instruction : block.instructions) {
            instruction.accept(this);
        }
    }

    @Override
    public void visit(LabelInstr labelInstr) {

    }

    @Override
    public void visit(Move_Reg_Integer move_reg_integer) {

    }

    @Override
    public void visit(Move_Reg_FuncName move_reg_funcName) {

    }

    @Override
    public void visit(Add add) {

    }

    @Override
    public void visit(Subtract subtract) {

    }

    @Override
    public void visit(Multiply multiply) {

    }

    @Override
    public void visit(LessThan lessThan) {

    }

    @Override
    public void visit(Load load) {

    }

    @Override
    public void visit(Store store) {

    }

    @Override
    public void visit(Move_Reg_Reg move_reg_reg) {

    }

    @Override
    public void visit(Move_Id_Reg move_id_reg) {
        if (!cStackRef.containsKey(move_id_reg.lhs.toString())) {
            cStackRef.put(move_id_reg.lhs.toString(), sp);
            sp -= 4;
        }
    }

    @Override
    public void visit(Move_Reg_Id move_reg_id) {

    }

    @Override
    public void visit(Alloc alloc) {

    }

    @Override
    public void visit(Print print) {

    }

    @Override
    public void visit(ErrorMessage errorMessage) {

    }

    @Override
    public void visit(Goto aGoto) {

    }

    @Override
    public void visit(IfGoto ifGoto) {

    }

    @Override
    public void visit(Call call) {

    }
}
