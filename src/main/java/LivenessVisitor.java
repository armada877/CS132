import Typecheck.Pair;
import cs132.IR.sparrow.*;
import cs132.IR.sparrow.visitor.Visitor;
import cs132.IR.token.Identifier;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class LivenessVisitor implements Visitor {
    public Map<String, Map<String, Pair<Integer, Integer>>> liveness;
    private int line;

    public LivenessVisitor() {
        liveness = new HashMap<>();
    }

    @Override
    public void visit(Program program) {
        for(FunctionDecl functionDecl : program.funDecls) {
            functionDecl.accept(this);
        }
    }

    @Override
    public void visit(FunctionDecl functionDecl) {
        line = 0;
        liveness.put(functionDecl.functionName.name, new HashMap<>());
        for (Identifier identifier : functionDecl.formalParameters) {
            liveness.get(functionDecl.functionName.toString()).put(identifier.toString(), new Pair<>(line, line));
        }
        line += 1;
        functionDecl.block.accept(this);
    }

    @Override
    public void visit(Block block) {
        for(Instruction instruction : block.instructions) {
            instruction.accept(this);
            line++;
        }
        String funcName = block.parent.functionName.name;
        String retId = block.return_id.toString();
        Pair<Integer, Integer> retIdLive = liveness.get(funcName).get(retId);

        liveness.get(funcName).put(retId, new Pair<>(retIdLive.fst, line));
    }

    @Override
    public void visit(LabelInstr labelInstr) {
        // TODO
    }

    @Override
    public void visit(Move_Id_Integer move_id_integer) {
        String funcName = move_id_integer.parent.parent.functionName.name;
        String assignId = move_id_integer.lhs.toString();

        if (!liveness.get(funcName).containsKey(assignId)){
            liveness.get(funcName).put(assignId, new Pair<>(line, line));
        }
    }

    @Override
    public void visit(Move_Id_FuncName move_id_funcName) {
        String funcName = move_id_funcName.parent.parent.functionName.name;
        String assignId = move_id_funcName.lhs.toString();

        if (!liveness.get(funcName).containsKey(assignId)){
            liveness.get(funcName).put(assignId, new Pair<>(line, line));
        }
    }

    @Override
    public void visit(Add add) {
        String funcName = add.parent.parent.functionName.name;
        String assignId = add.lhs.toString();
        String arg1 = add.arg1.toString();
        String arg2 = add.arg2.toString();
        Pair<Integer, Integer> arg1Live = liveness.get(funcName).get(arg1);
        Pair<Integer, Integer> arg2Live = liveness.get(funcName).get(arg2);

        if (!liveness.get(funcName).containsKey(assignId)){
            liveness.get(funcName).put(assignId, new Pair<>(line, line));
        }

        liveness.get(funcName).put(arg1, new Pair<>(arg1Live.fst, line));
        liveness.get(funcName).put(arg2, new Pair<>(arg2Live.fst, line));

    }

    @Override
    public void visit(Subtract subtract) {
        // TODO
        String funcName = subtract.parent.parent.functionName.name;
        String assignId = subtract.lhs.toString();
        String arg1 = subtract.arg1.toString();
        String arg2 = subtract.arg2.toString();
        Pair<Integer, Integer> arg1Live = liveness.get(funcName).get(arg1);
        Pair<Integer, Integer> arg2Live = liveness.get(funcName).get(arg2);

        if (!liveness.get(funcName).containsKey(assignId)){
            liveness.get(funcName).put(assignId, new Pair<>(line, line));
        }

        liveness.get(funcName).put(arg1, new Pair<>(arg1Live.fst, line));
        liveness.get(funcName).put(arg2, new Pair<>(arg2Live.fst, line));
    }

    @Override
    public void visit(Multiply multiply) {
        // TODO
        String funcName = multiply.parent.parent.functionName.name;
        String assignId = multiply.lhs.toString();
        String arg1 = multiply.arg1.toString();
        String arg2 = multiply.arg2.toString();
        Pair<Integer, Integer> arg1Live = liveness.get(funcName).get(arg1);
        Pair<Integer, Integer> arg2Live = liveness.get(funcName).get(arg2);

        if (!liveness.get(funcName).containsKey(assignId)){
            liveness.get(funcName).put(assignId, new Pair<>(line, line + 1));
        }

        liveness.get(funcName).put(arg1, new Pair<>(arg1Live.fst, line));
        liveness.get(funcName).put(arg2, new Pair<>(arg2Live.fst, line));
    }

    @Override
    public void visit(LessThan lessThan) {
        // TODO
        String funcName = lessThan.parent.parent.functionName.name;
        String assignId = lessThan.lhs.toString();
        String arg1 = lessThan.arg1.toString();
        String arg2 = lessThan.arg2.toString();
        Pair<Integer, Integer> arg1Live = liveness.get(funcName).get(arg1);
        Pair<Integer, Integer> arg2Live = liveness.get(funcName).get(arg2);

        if (!liveness.get(funcName).containsKey(assignId)){
            liveness.get(funcName).put(assignId, new Pair<>(line, line));
        }

        liveness.get(funcName).put(arg1, new Pair<>(arg1Live.fst, line));
        liveness.get(funcName).put(arg2, new Pair<>(arg2Live.fst, line));
    }

    @Override
    public void visit(Load load) {
        // TODO
        String funcName = load.parent.parent.functionName.name;
        String assignId = load.lhs.toString();
        String base = load.base.toString();
        Pair<Integer, Integer> baseLive = liveness.get(funcName).get(base);

        if (!liveness.get(funcName).containsKey(assignId)){
            liveness.get(funcName).put(assignId, new Pair<>(line, line));
        }

        liveness.get(funcName).put(base, new Pair<>(baseLive.fst, line));
    }

    @Override
    public void visit(Store store) {
        // TODO
        String funcName = store.parent.parent.functionName.name;
        String base = store.base.toString();
        String rhs = store.rhs.toString();
        Pair<Integer, Integer> baseLive = liveness.get(funcName).get(base);
        Pair<Integer, Integer> rhsLive = liveness.get(funcName).get(rhs);

        liveness.get(funcName).put(base, new Pair<>(baseLive.fst, line));
        liveness.get(funcName).put(rhs, new Pair<>(rhsLive.fst, line));
    }

    @Override
    public void visit(Move_Id_Id move_id_id) {
        String funcName = move_id_id.parent.parent.functionName.name;
        String assignId = move_id_id.lhs.toString();
        String rhs = move_id_id.rhs.toString();
        Pair<Integer, Integer> rhsLive = liveness.get(funcName).get(rhs);

        if (!liveness.get(funcName).containsKey(assignId)){
            liveness.get(funcName).put(assignId, new Pair<>(line, line));
        }

        liveness.get(funcName).put(rhs, new Pair<>(rhsLive.fst, line));
    }

    @Override
    public void visit(Alloc alloc) {
        // TODO
        String funcName = alloc.parent.parent.functionName.name;
        String assignId = alloc.lhs.toString();
        String size = alloc.size.toString();
        Pair<Integer, Integer> sizeLive = liveness.get(funcName).get(size);

        if (!liveness.get(funcName).containsKey(assignId)){
            liveness.get(funcName).put(assignId, new Pair<>(line, line));
        }

        liveness.get(funcName).put(size, new Pair<>(sizeLive.fst, line));
    }

    @Override
    public void visit(Print print) {
        // TODO
        String funcName = print.parent.parent.functionName.name;
        String content = print.content.toString();
        Pair<Integer, Integer> contentLive = liveness.get(funcName).get(content);

        liveness.get(funcName).put(content, new Pair<>(contentLive.fst, line));
    }

    @Override
    public void visit(ErrorMessage errorMessage) {
        // TODO
    }

    @Override
    public void visit(Goto aGoto) {
        // TODO
    }

    @Override
    public void visit(IfGoto ifGoto) {
        // TODO
        String funcName = ifGoto.parent.parent.functionName.name;
        String condition = ifGoto.condition.toString();
        Pair<Integer, Integer> conditionLive = liveness.get(funcName).get(condition);

        liveness.get(funcName).put(condition, new Pair<>(conditionLive.fst, line));
    }

    @Override
    public void visit(Call call) {
        // TODO
        String funcName = call.parent.parent.functionName.name;
        String assignId = call.lhs.toString();

        if (!liveness.get(funcName).containsKey(assignId)){
            liveness.get(funcName).put(assignId, new Pair<>(line, line));
        }

        List<Identifier> params = call.args;
        for (Identifier param : params) {
            Pair<Integer, Integer> paramLive = liveness.get(funcName).get(param.toString());
            liveness.get(funcName).put(param.toString(), new Pair<>(paramLive.fst, line));
        }
    }
}
