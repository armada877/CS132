import Typecheck.Pair;
import cs132.IR.sparrow.*;
import cs132.IR.sparrow.Add;
import cs132.IR.sparrow.Alloc;
import cs132.IR.sparrow.Block;
import cs132.IR.sparrow.Call;
import cs132.IR.sparrow.ErrorMessage;
import cs132.IR.sparrow.FunctionDecl;
import cs132.IR.sparrow.Goto;
import cs132.IR.sparrow.IfGoto;
import cs132.IR.sparrow.Instruction;
import cs132.IR.sparrow.LabelInstr;
import cs132.IR.sparrow.LessThan;
import cs132.IR.sparrow.Load;
import cs132.IR.sparrow.Multiply;
import cs132.IR.sparrow.Print;
import cs132.IR.sparrow.Program;
import cs132.IR.sparrow.Store;
import cs132.IR.sparrow.Subtract;
import cs132.IR.sparrow.visitor.Visitor;
import cs132.IR.sparrowv.*;
import cs132.IR.token.FunctionName;
import cs132.IR.token.Identifier;
import cs132.IR.token.Register;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class TranslateVisitor implements Visitor {
    public List<cs132.IR.sparrowv.FunctionDecl> functionDecls;
    public List<cs132.IR.sparrowv.Instruction> currentInstructions;
    public LinearScanRegisterAllocator linearScanRegisterAllocator;
    public LivenessVisitor livenessVisitor;
    public Map<String, String> currentRegAllocations;
    private int line;

    public TranslateVisitor(LinearScanRegisterAllocator linearScanRegisterAllocator, LivenessVisitor livenessVisitor){
        this.linearScanRegisterAllocator = linearScanRegisterAllocator;
        this.livenessVisitor = livenessVisitor;
        functionDecls = new ArrayList<>();
        currentInstructions = new ArrayList<>();
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
        currentInstructions = new ArrayList<>();
        currentRegAllocations = linearScanRegisterAllocator.registerAllocations.get(functionDecl.functionName.toString());

        List<Identifier> svParamsList = new ArrayList<>();
        int count = 2;
        for (Identifier id : functionDecl.formalParameters) {
            if (count > 7) {
                svParamsList.add(id);
            }
            count++;
        }
        line += 1;

        functionDecl.block.accept(this);

        Identifier retId = functionDecl.block.return_id;
        Register regReg;
        if (currentRegAllocations.get(retId.toString()) != null) {
            regReg = new Register(currentRegAllocations.get(retId.toString()));
            currentInstructions.add(new Move_Id_Reg(retId, regReg));
        }
        cs132.IR.sparrowv.Block svBlock = new cs132.IR.sparrowv.Block(currentInstructions, retId);
        cs132.IR.sparrowv.FunctionDecl svFunctionDecl = new cs132.IR.sparrowv.FunctionDecl(functionDecl.functionName, svParamsList, svBlock);
        functionDecls.add(svFunctionDecl);
    }

    @Override
    public void visit(Block block) {
        for (Instruction instruction : block.instructions) {
            instruction.accept(this);
            line++;
        }
    }

    @Override
    public void visit(LabelInstr labelInstr) {
        currentInstructions.add(new cs132.IR.sparrowv.LabelInstr(labelInstr.label));
    }

    @Override
    public void visit(Move_Id_Integer move_id_integer) {
        String lhs = move_id_integer.lhs.toString();
        if (currentRegAllocations.get(lhs) == null) {
            currentInstructions.add(new Move_Reg_Integer(new Register("s11"), move_id_integer.rhs));
            currentInstructions.add(new Move_Id_Reg(move_id_integer.lhs, new Register("s11")));
        } else {
            currentInstructions.add(new Move_Reg_Integer(new Register(currentRegAllocations.get(lhs)), move_id_integer.rhs));
        }
    }

    @Override
    public void visit(Move_Id_FuncName move_id_funcName) {
        String lhs = move_id_funcName.lhs.toString();
        FunctionName functionName = move_id_funcName.rhs;

        if (currentRegAllocations.get(lhs) == null) {
            currentInstructions.add(new Move_Reg_FuncName(new Register("s11"), move_id_funcName.rhs));
            currentInstructions.add(new Move_Id_Reg(move_id_funcName.lhs, new Register("s11")));
        } else {
            currentInstructions.add(new Move_Reg_FuncName(new Register(currentRegAllocations.get(lhs)), move_id_funcName.rhs));
        }
    }

    @Override
    public void visit(Add add) {
        Identifier lhs = add.lhs;
        Identifier arg1 = add.arg1;
        Identifier arg2 = add.arg2;
        Register lhsReg = new Register("s11");
        Register arg1Reg = new Register("s10");
        Register arg2Reg = new Register("s11");

        if (currentRegAllocations.get(arg1.toString()) == null) {
            currentInstructions.add(new Move_Reg_Id(arg1Reg, arg1));
        } else {
            arg1Reg = new Register(currentRegAllocations.get(arg1.toString()));
        }

        if (currentRegAllocations.get(arg2.toString()) == null) {
            currentInstructions.add(new Move_Reg_Id(arg2Reg, arg2));
        } else {
            arg2Reg = new Register(currentRegAllocations.get(arg2.toString()));
        }

        if (currentRegAllocations.get(lhs.toString()) == null) {
            currentInstructions.add(new cs132.IR.sparrowv.Add(lhsReg, arg1Reg, arg2Reg));
            currentInstructions.add(new Move_Id_Reg(lhs, lhsReg));
        } else {
            lhsReg = new Register(currentRegAllocations.get(lhs.toString()));
            currentInstructions.add(new cs132.IR.sparrowv.Add(lhsReg, arg1Reg, arg2Reg));
        }
    }

    @Override
    public void visit(Subtract subtract) {
        Identifier lhs = subtract.lhs;
        Identifier arg1 = subtract.arg1;
        Identifier arg2 = subtract.arg2;
        Register lhsReg = new Register("s11");
        Register arg1Reg = new Register("s10");
        Register arg2Reg = new Register("s11");

        if (currentRegAllocations.get(arg1.toString()) == null) {
            currentInstructions.add(new Move_Reg_Id(arg1Reg, arg1));
        } else {
            arg1Reg = new Register(currentRegAllocations.get(arg1.toString()));
        }

        if (currentRegAllocations.get(arg2.toString()) == null) {
            currentInstructions.add(new Move_Reg_Id(arg2Reg, arg2));
        } else {
            arg2Reg = new Register(currentRegAllocations.get(arg2.toString()));
        }

        if (currentRegAllocations.get(lhs.toString()) == null) {
            currentInstructions.add(new cs132.IR.sparrowv.Subtract(lhsReg, arg1Reg, arg2Reg));
            currentInstructions.add(new Move_Id_Reg(lhs, lhsReg));
        } else {
            lhsReg = new Register(currentRegAllocations.get(lhs.toString()));
            currentInstructions.add(new cs132.IR.sparrowv.Subtract(lhsReg, arg1Reg, arg2Reg));
        }
    }

    @Override
    public void visit(Multiply multiply) {
        Identifier lhs = multiply.lhs;
        Identifier arg1 = multiply.arg1;
        Identifier arg2 = multiply.arg2;
        Register lhsReg = new Register("s11");
        Register arg1Reg = new Register("s10");
        Register arg2Reg = new Register("s11");

        if (currentRegAllocations.get(arg1.toString()) == null) {
            currentInstructions.add(new Move_Reg_Id(arg1Reg, arg1));
        } else {
            arg1Reg = new Register(currentRegAllocations.get(arg1.toString()));
        }

        if (currentRegAllocations.get(arg2.toString()) == null) {
            currentInstructions.add(new Move_Reg_Id(arg2Reg, arg2));
        } else {
            arg2Reg = new Register(currentRegAllocations.get(arg2.toString()));
        }

        if (currentRegAllocations.get(lhs.toString()) == null) {
            currentInstructions.add(new cs132.IR.sparrowv.Multiply(lhsReg, arg1Reg, arg2Reg));
            currentInstructions.add(new Move_Id_Reg(lhs, lhsReg));
        } else {
            lhsReg = new Register(currentRegAllocations.get(lhs.toString()));
            currentInstructions.add(new cs132.IR.sparrowv.Multiply(lhsReg, arg1Reg, arg2Reg));
        }
    }

    @Override
    public void visit(LessThan lessThan) {
        Identifier lhs = lessThan.lhs;
        Identifier arg1 = lessThan.arg1;
        Identifier arg2 = lessThan.arg2;
        Register lhsReg = new Register("s11");
        Register arg1Reg = new Register("s10");
        Register arg2Reg = new Register("s11");

        if (currentRegAllocations.get(arg1.toString()) == null) {
            currentInstructions.add(new Move_Reg_Id(arg1Reg, arg1));
        } else {
            arg1Reg = new Register(currentRegAllocations.get(arg1.toString()));
        }

        if (currentRegAllocations.get(arg2.toString()) == null) {
            currentInstructions.add(new Move_Reg_Id(arg2Reg, arg2));
        } else {
            arg2Reg = new Register(currentRegAllocations.get(arg2.toString()));
        }

        if (currentRegAllocations.get(lhs.toString()) == null) {
            currentInstructions.add(new cs132.IR.sparrowv.LessThan(lhsReg, arg1Reg, arg2Reg));
            currentInstructions.add(new Move_Id_Reg(lhs, lhsReg));
        } else {
            lhsReg = new Register(currentRegAllocations.get(lhs.toString()));
            currentInstructions.add(new cs132.IR.sparrowv.LessThan(lhsReg, arg1Reg, arg2Reg));
        }
    }

    @Override
    public void visit(Load load) {
        Identifier lhs = load.lhs;
        Identifier base = load.base;
        Register lhsReg = new Register("s11");
        Register baseReg = new Register("s10");

        if (currentRegAllocations.get(base.toString()) == null) {
            currentInstructions.add(new Move_Reg_Id(baseReg, base));
        } else {
            baseReg = new Register(currentRegAllocations.get(base.toString()));
        }

        if (currentRegAllocations.get(lhs.toString()) == null) {
            currentInstructions.add(new cs132.IR.sparrowv.Load(lhsReg, baseReg, load.offset));
            currentInstructions.add(new Move_Id_Reg(lhs, lhsReg));
        } else {
            lhsReg = new Register(currentRegAllocations.get(lhs.toString()));
            currentInstructions.add(new cs132.IR.sparrowv.Load(lhsReg, baseReg, load.offset));
        }
    }

    @Override
    public void visit(Store store) {
        Identifier base = store.base;
        Identifier rhs = store.rhs;
        Register baseReg = new Register("s11");
        Register rhsReg = new Register("s10");

        if (currentRegAllocations.get(base.toString()) == null) {
            currentInstructions.add(new Move_Reg_Id(baseReg, base));
        } else {
            baseReg = new Register(currentRegAllocations.get(base.toString()));
        }

        if (currentRegAllocations.get(rhs.toString()) == null) {
            currentInstructions.add(new Move_Reg_Id(rhsReg, rhs));
        } else {
            rhsReg = new Register(currentRegAllocations.get(rhs.toString()));
        }

        currentInstructions.add(new cs132.IR.sparrowv.Store(baseReg, store.offset, rhsReg));
    }

    @Override
    public void visit(Move_Id_Id move_id_id) {
        Identifier lhs = move_id_id.lhs;
        Identifier rhs = move_id_id.rhs;
        Register lhsReg = new Register("s11");
        Register rhsReg = new Register("s10");

        if (currentRegAllocations.get(rhs.toString()) == null) {
            currentInstructions.add(new Move_Reg_Id(rhsReg, rhs));
        } else {
            rhsReg = new Register(currentRegAllocations.get(rhs.toString()));
        }

        if (currentRegAllocations.get(lhs.toString()) == null) {
            currentInstructions.add(new Move_Reg_Reg(lhsReg, rhsReg));
            currentInstructions.add(new Move_Id_Reg(lhs, lhsReg));
        } else {
            lhsReg = new Register(currentRegAllocations.get(lhs.toString()));
            currentInstructions.add(new Move_Reg_Reg(lhsReg, rhsReg));
        }
    }

    @Override
    public void visit(Alloc alloc) {
        Identifier lhs = alloc.lhs;
        Identifier size = alloc.size;
        Register lhsReg = new Register("s11");
        Register sizeReg = new Register("s10");

        if (currentRegAllocations.get(size.toString()) == null) {
            currentInstructions.add(new Move_Reg_Id(sizeReg, size));
        } else {
            sizeReg = new Register(currentRegAllocations.get(size.toString()));
        }

        if (currentRegAllocations.get(lhs.toString()) == null) {
            currentInstructions.add(new cs132.IR.sparrowv.Alloc(lhsReg, sizeReg));
            currentInstructions.add(new Move_Id_Reg(lhs, lhsReg));
        } else {
            lhsReg = new Register(currentRegAllocations.get(lhs.toString()));
            currentInstructions.add(new cs132.IR.sparrowv.Alloc(lhsReg, sizeReg));
        }
    }

    @Override
    public void visit(Print print) {
        Identifier content = print.content;
        Register contentReg = new Register("s11");

        if (currentRegAllocations.get(content.toString()) == null) {
            currentInstructions.add(new Move_Reg_Id(contentReg, content));
            currentInstructions.add(new cs132.IR.sparrowv.Print(contentReg));
        } else {
            contentReg = new Register(currentRegAllocations.get(content.toString()));
            currentInstructions.add(new cs132.IR.sparrowv.Print(contentReg));
        }
    }

    @Override
    public void visit(ErrorMessage errorMessage) {
        currentInstructions.add(new cs132.IR.sparrowv.ErrorMessage(errorMessage.msg));
    }

    @Override
    public void visit(Goto aGoto) {
        currentInstructions.add(new cs132.IR.sparrowv.Goto(aGoto.label));
    }

    @Override
    public void visit(IfGoto ifGoto) {
        Identifier condition = ifGoto.condition;
        Register conditionReg = new Register("s11");

        if (currentRegAllocations.get(condition.toString()) == null) {
            currentInstructions.add(new Move_Reg_Id(conditionReg, condition));
            currentInstructions.add(new cs132.IR.sparrowv.IfGoto(conditionReg, ifGoto.label));
        } else {
            conditionReg = new Register(currentRegAllocations.get(condition.toString()));
            currentInstructions.add(new cs132.IR.sparrowv.IfGoto(conditionReg, ifGoto.label));
        }
    }

    @Override
    public void visit(Call call) {
        // Find Vars live before and after call
        Map<String, Pair<Integer, Integer>> currentLiveTable = livenessVisitor.liveness.get(call.parent.parent.functionName.toString());
        List<String> saveVars = new ArrayList();
        for (Map.Entry<String, Pair<Integer, Integer>> varEntry : currentLiveTable.entrySet()) {
            if (varEntry.getValue().fst < line && varEntry.getValue().snd > line) {
                saveVars.add(varEntry.getKey());
            }
        }

        // Stack Save
        List<Register> savedRegs = new ArrayList<>();
        for (String var : saveVars) {
            if (currentRegAllocations.get(var) != null) {
                Identifier stackSave = new Identifier("stackSave_" + currentRegAllocations.get(var));
                Register saveReg = new Register(currentRegAllocations.get(var));
                savedRegs.add(saveReg);
                currentInstructions.add(new Move_Id_Reg(stackSave, saveReg));
            }
        }

        // Set Params
        int counter = 2;
        List<Identifier> svParams = new ArrayList<>();
        for (Identifier param : call.args) {
            if (counter <= 7) {
                counter++;
                continue;
            }
            if (currentRegAllocations.get(param.toString()) == null) {
                svParams.add(param);
            } else {
                Register paramReg = new Register(currentRegAllocations.get(param.toString()));
                Identifier paramId = new Identifier("param_"+paramReg.toString());
                currentInstructions.add(new Move_Id_Reg(paramId, paramReg));
                svParams.add(paramId);
            }
        }
        counter = 2;
        for (Identifier param : call.args) {
            if (counter <= 7) {
                Register paramReg = new Register("a"+counter);
                if (currentRegAllocations.get(param.toString()) == null) {
                    currentInstructions.add(new Move_Reg_Id(paramReg, param));
                } else {
                    Register pReg = new Register(currentRegAllocations.get(param.toString()));
                    currentInstructions.add(new Move_Reg_Reg(paramReg, pReg));
                }
                counter++;
            }
        }

        // Make Call
        Identifier lhs = call.lhs;
        Identifier callee = call.callee;
        Register lhsReg = new Register("s11");
        Register calleeReg = new Register("s10");

        if (currentRegAllocations.get(callee.toString()) == null) {
            currentInstructions.add(new Move_Reg_Id(calleeReg, callee));
        } else {
            calleeReg = new Register(currentRegAllocations.get(callee.toString()));
        }

        if (currentRegAllocations.get(lhs.toString()) == null) {
            currentInstructions.add(new cs132.IR.sparrowv.Call(lhsReg, calleeReg, svParams));
            currentInstructions.add(new Move_Id_Reg(lhs, lhsReg));
        } else {
            lhsReg = new Register(currentRegAllocations.get(lhs.toString()));
            currentInstructions.add(new cs132.IR.sparrowv.Call(lhsReg, calleeReg, svParams));
        }

        // Stack Load
        for (Register savedReg : savedRegs) {
            currentInstructions.add(new Move_Reg_Id(savedReg, new Identifier("stackSave_"+savedReg.toString())));
        }
    }
}
