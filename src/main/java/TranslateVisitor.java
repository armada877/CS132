import cs132.IR.sparrowv.*;
import cs132.IR.sparrowv.visitor.RetVisitor;

public class TranslateVisitor implements RetVisitor {
    @Override
    public Object visit(Program program) {
        return null;
    }

    @Override
    public Object visit(FunctionDecl functionDecl) {
        return null;
    }

    @Override
    public Object visit(Block block) {
        return null;
    }

    @Override
    public Object visit(LabelInstr labelInstr) {
        return null;
    }

    @Override
    public Object visit(Move_Reg_Integer move_reg_integer) {
        return null;
    }

    @Override
    public Object visit(Move_Reg_FuncName move_reg_funcName) {
        return null;
    }

    @Override
    public Object visit(Add add) {
        return null;
    }

    @Override
    public Object visit(Subtract subtract) {
        return null;
    }

    @Override
    public Object visit(Multiply multiply) {
        return null;
    }

    @Override
    public Object visit(LessThan lessThan) {
        return null;
    }

    @Override
    public Object visit(Load load) {
        return null;
    }

    @Override
    public Object visit(Store store) {
        return null;
    }

    @Override
    public Object visit(Move_Reg_Reg move_reg_reg) {
        return null;
    }

    @Override
    public Object visit(Move_Id_Reg move_id_reg) {
        return null;
    }

    @Override
    public Object visit(Move_Reg_Id move_reg_id) {
        return null;
    }

    @Override
    public Object visit(Alloc alloc) {
        return null;
    }

    @Override
    public Object visit(Print print) {
        return null;
    }

    @Override
    public Object visit(ErrorMessage errorMessage) {
        return null;
    }

    @Override
    public Object visit(Goto aGoto) {
        return null;
    }

    @Override
    public Object visit(IfGoto ifGoto) {
        return null;
    }

    @Override
    public Object visit(Call call) {
        return null;
    }
}
