package S2V;

import Typecheck.Pair;
import cs132.IR.sparrow.*;
import cs132.IR.sparrow.visitor.Visitor;
import cs132.IR.token.Identifier;

import java.util.*;

public class LivenessVisitor implements Visitor {
    public Map<String, Map<String, Pair<Integer, Integer>>> liveness;
    public Map<String, Map<String, Pair<Integer, Integer>>> paramLiveness;
    public Map<String, Set<String>> labelVars;
    public List<String> activeLabels;
    private int line;

    public LivenessVisitor() {
        liveness = new HashMap<>();
        paramLiveness = new HashMap<>();
        activeLabels = new ArrayList<>();
        labelVars = new HashMap<>();
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
        labelVars = new HashMap<>();
        activeLabels = new ArrayList<>();
        liveness.put(functionDecl.functionName.name, new HashMap<>());
        int numParams = functionDecl.formalParameters.size();
        numParams = numParams * -1;
        for (Identifier identifier : functionDecl.formalParameters) {
            liveness.get(functionDecl.functionName.toString()).put(identifier.toString(), new Pair<>(numParams, line));
            numParams++;
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
        activeLabels.add(labelInstr.label.toString());
        labelVars.put(labelInstr.label.toString(), new TreeSet<>());
    }

    @Override
    public void visit(Move_Id_Integer move_id_integer) {
        String funcName = move_id_integer.parent.parent.functionName.name;
        String assignId = move_id_integer.lhs.toString();

        addVarToLabels(assignId);

        if (!liveness.get(funcName).containsKey(assignId)){
            liveness.get(funcName).put(assignId, new Pair<>(line, line));
        }
    }

    @Override
    public void visit(Move_Id_FuncName move_id_funcName) {
        String funcName = move_id_funcName.parent.parent.functionName.name;
        String assignId = move_id_funcName.lhs.toString();

        addVarToLabels(assignId);

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

        addVarToLabels(assignId);
        addVarToLabels(arg1);
        addVarToLabels(arg2);

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

        addVarToLabels(assignId);
        addVarToLabels(arg1);
        addVarToLabels(arg2);

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

        addVarToLabels(assignId);
        addVarToLabels(arg1);
        addVarToLabels(arg2);

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

        addVarToLabels(assignId);
        addVarToLabels(arg1);
        addVarToLabels(arg2);

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

        addVarToLabels(assignId);
        addVarToLabels(base);

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

        addVarToLabels(base);
        addVarToLabels(rhs);

        liveness.get(funcName).put(base, new Pair<>(baseLive.fst, line));
        liveness.get(funcName).put(rhs, new Pair<>(rhsLive.fst, line));
    }

    @Override
    public void visit(Move_Id_Id move_id_id) {
        String funcName = move_id_id.parent.parent.functionName.name;
        String assignId = move_id_id.lhs.toString();
        String rhs = move_id_id.rhs.toString();
        Pair<Integer, Integer> rhsLive = liveness.get(funcName).get(rhs);

        addVarToLabels(assignId);
        addVarToLabels(rhs);

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

        addVarToLabels(assignId);
        addVarToLabels(size);

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

        addVarToLabels(content);

        liveness.get(funcName).put(content, new Pair<>(contentLive.fst, line));
    }

    @Override
    public void visit(ErrorMessage errorMessage) {
        // TODO
    }

    @Override
    public void visit(Goto aGoto) {
        // TODO
        String funcName = aGoto.parent.parent.functionName.name;
        String label = aGoto.label.toString();
        activeLabels.remove(label);

        if (labelVars.containsKey(label)) {
            for (String var : labelVars.get(label)) {
                Pair<Integer, Integer> varLive = liveness.get(funcName).get(var);

                liveness.get(funcName).put(var, new Pair<>(varLive.fst, line));
            }
        }
    }

    @Override
    public void visit(IfGoto ifGoto) {
        // TODO
        String funcName = ifGoto.parent.parent.functionName.name;
        String condition = ifGoto.condition.toString();
        Pair<Integer, Integer> conditionLive = liveness.get(funcName).get(condition);

        addVarToLabels(condition);

        liveness.get(funcName).put(condition, new Pair<>(conditionLive.fst, line));
    }

    @Override
    public void visit(Call call) {
        // TODO
        String funcName = call.parent.parent.functionName.name;
        String assignId = call.lhs.toString();
        String callee = call.callee.toString();

        addVarToLabels(assignId);
        addVarToLabels(callee);

        if (!liveness.get(funcName).containsKey(assignId)){
            liveness.get(funcName).put(assignId, new Pair<>(line, line));
        }

        List<Identifier> params = call.args;
        for (Identifier param : params) {
            Pair<Integer, Integer> paramLive = liveness.get(funcName).get(param.toString());
            liveness.get(funcName).put(param.toString(), new Pair<>(paramLive.fst, line));
            addVarToLabels(param.toString());
        }

        Pair<Integer, Integer> calleeLive = liveness.get(funcName).get(callee);
        liveness.get(funcName).put(callee, new Pair<>(calleeLive.fst, line));
    }

    private void addVarToLabels(String var) {
        for (String label : activeLabels) {
            labelVars.get(label).add(var);
        }
    }
}
