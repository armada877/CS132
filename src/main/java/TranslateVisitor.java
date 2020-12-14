import cs132.IR.sparrowv.*;
import cs132.IR.sparrowv.visitor.RetVisitor;
import cs132.IR.token.Identifier;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class TranslateVisitor implements RetVisitor<List<String>> {
    public Map<String, Map<String, Integer>> stackRef;
    public List<String> program;
    private String currentFunc;
    private Map<String, Integer> cStackRef;
    Integer endNo = 0;

    public TranslateVisitor(Map<String, Map<String, Integer>> sr) {
        stackRef = sr;
        program = new ArrayList<>();
    }

    @Override
    public List<String> visit(Program program) {
        String preamble = "  .equiv @sbrk, 9\n" +
                "  .equiv @print_string, 4\n" +
                "  .equiv @print_char, 11\n" +
                "  .equiv @print_int, 1\n" +
                "  .equiv @exit 10\n" +
                "  .equiv @exit2, 17\n" +
                "\n" +
                "\n" +
                ".text\n" +
                "\n" +
                "  jal Main\n" +
                "  li a0, @exit\n" +
                "  ecall\n\n";
        this.program.add(preamble);

        for (FunctionDecl functionDecl : program.funDecls) {
            functionDecl.accept(this);
        }

        String epilogue =
                ".globl error\n" +
                "error:\n" +
                "  mv a1, a0\n" +
                "  li a0, @print_string\n" +
                "  ecall\n" +
                "  li a1, 10\n" +
                "  li a0, @print_char\n" +
                "  ecall\n" +
                "  li a0, @exit\n" +
                "  ecall\n" +
                "abort_17:\n" +
                "  j abort_17\n" +
                "\n" +
                "\n" +
                ".globl alloc\n" +
                "alloc:\n" +
                "  mv a1, a0\n" +
                "  li a0, @sbrk\n" +
                "  ecall\n" +
                "  jr ra\n" +
                "\n" +
                "\n" +
                ".data\n" +
                "\n" +
                ".globl msg_0\n" +
                "msg_0:\n" +
                "  .asciiz \"null pointer\"\n" +
                "  .align 2\n"+
                ".globl msg_1\n" +
                "msg_1:\n" +
                "  .asciiz \"array index out of bounds\"\n" +
                "  .align 2\n";

        this.program.add(epilogue);
        return null;
    }

    @Override
    public List<String> visit(FunctionDecl functionDecl) {
        currentFunc = functionDecl.functionName.name;
        cStackRef = stackRef.get(currentFunc);
        Integer stackSize = -12;
        for (Map.Entry<String, Integer> e : cStackRef.entrySet()) {
            if (e.getValue() < stackSize) {
                stackSize = e.getValue();
            }
        }
        stackSize = -1 * stackSize;

        String preamble = ".globl " + currentFunc + "\n" +
                currentFunc + ":\n" +
                "  sw fp, -8(sp)\n" +
                "  mv fp, sp\n" +
                "  li t6, " + stackSize.toString() + "\n" +
                "  sub sp, sp, t6\n" +
                "  sw ra, -4(fp) ";
        program.add(preamble);

        // TODO: Setup stack pointer, etc for function
        functionDecl.block.accept(this);
        // TODO: put return value into return register
        // TODO: deallocate calle stuff and return frame pointer, stack pointer to original place
        Integer ret = cStackRef.get(functionDecl.block.return_id.toString());
        Integer argSpace = functionDecl.formalParameters.size() * 4;
        String epilogue = "lw a0, " + ret + "(fp)\n" +
                "  lw ra, -4(fp)\n" +
                "  lw fp, -8(fp)\n" +
                "  addi sp, sp, " + stackSize.toString() + "\n" +
                "  addi sp, sp, " + argSpace.toString() + "\n" +
                "  jr ra\n";
        program.add(epilogue);
        return null;
    }

    @Override
    public List<String> visit(Block block) {
        for (Instruction instruction : block.instructions) {
            instruction.accept(this);
        }
        return null;
    }

    @Override
    public List<String> visit(LabelInstr labelInstr) {
        String instr = currentFunc+"_"+labelInstr.label.toString()+":\n";
        program.add(instr);
        return null;
    }

    @Override
    public List<String> visit(Move_Reg_Integer move_reg_integer) {
        String instr = "LI " + move_reg_integer.lhs.toString() + ", " + (new Integer(move_reg_integer.rhs)).toString() + "\n";
        program.add(instr);
        return null;
    }

    @Override
    public List<String> visit(Move_Reg_FuncName move_reg_funcName) {
        String instr = "la " + move_reg_funcName.lhs.toString() + ", " + move_reg_funcName.rhs.name + "\n";
        program.add(instr);
        return null;
    }

    @Override
    public List<String> visit(Add add) {
        String instr = threeOp("ADD", add.lhs.toString(), add.arg1.toString(), add.arg2.toString());
        program.add(instr);
        return null;
    }

    @Override
    public List<String> visit(Subtract subtract) {
        String instr = threeOp("SUB", subtract.lhs.toString(), subtract.arg1.toString(), subtract.arg2.toString());
        program.add(instr);
        return null;
    }

    @Override
    public List<String> visit(Multiply multiply) {
        String instr = threeOp("MUL", multiply.lhs.toString(), multiply.arg1.toString(), multiply.arg2.toString());
        program.add(instr);
        return null;
    }

    @Override
    public List<String> visit(LessThan lessThan) {
        String instr = threeOp("SLT", lessThan.lhs.toString(), lessThan.arg1.toString(), lessThan.arg2.toString());
        program.add(instr);
        return null;
    }

    @Override
    public List<String> visit(Load load) {
        String instr = "LW " + load.lhs.toString() + ", " + (new Integer(load.offset).toString()) + "(" + load.base.toString() + ")\n";
        program.add(instr);
        return null;
    }

    @Override
    public List<String> visit(Store store) {
        String instr = "SW " + store.rhs.toString() + ", " + (new Integer(store.offset).toString()) + "(" + store.base.toString() +")\n";
        program.add(instr);
        return null;
    }

    @Override
    public List<String> visit(Move_Reg_Reg move_reg_reg) {
        String instr = threeOp("ADDI", move_reg_reg.lhs.toString(), move_reg_reg.rhs.toString(), (new Integer(0)).toString());
        program.add(instr);
        return null;
    }

    @Override
    public List<String> visit(Move_Id_Reg move_id_reg) {
        // TODO: lookup from stack reference and store word
        // Store reg into stack
        String instr = "SW " + move_id_reg.rhs.toString() + ", " + (new Integer(cStackRef.get(move_id_reg.lhs.toString())).toString()) + "(fp)\n";
        program.add(instr);
        return null;
    }

    @Override
    public List<String> visit(Move_Reg_Id move_reg_id) {
        // TODO: lookup from stack reference and load word
        String instr = "LW " + move_reg_id.lhs.toString() + ", " + (new Integer(cStackRef.get(move_reg_id.rhs.toString())).toString()) + "(fp)\n";
        program.add(instr);
        return null;
    }

    @Override
    public List<String> visit(Alloc alloc) {
        // TODO: see ccle for code
        String preamble = "MV a0, " + alloc.size.toString() + "\n";
        String instr = "JAL alloc\n";
        String epilogue = "MV " + alloc.lhs.toString() + ", a0\n";
        program.add(preamble);
        program.add(instr);
        program.add(epilogue);
        return null;
    }

    @Override
    public List<String> visit(Print print) {
        // TODO: see ccle for code
        String preamble = "MV a1, " + print.content.toString() + "\n";
        String instr = "LI a0, @print_int\n" +
                "ecall\n" +
                "li a1, 10\n" +
                "  li a0, @print_char\n" +
                "  ecall\n";
        program.add(preamble);
        program.add(instr);
        return null;
    }

    @Override
    public List<String> visit(ErrorMessage errorMessage) {
        // TODO: see ccle for code
        if (errorMessage.msg == "null pointer"){
            String instr = "  la a0, msg_0\n" +
                    "  j error\n";
            program.add(instr);
        } else {
            String instr = "  la a0, msg_1\n" +
                    "  j error\n";
            program.add(instr);
        }
        return null;
    }

    @Override
    public List<String> visit(Goto aGoto) {
        String instr = "J " + currentFunc+"_"+aGoto.label.toString()+"\n";
        program.add(instr);
        return null;
    }

    @Override
    public List<String> visit(IfGoto ifGoto) {
        String instr = "BNEZ " + ifGoto.condition.toString() + ", " + "end" + endNo.toString() + "\n" +
                "J " + currentFunc+"_"+ifGoto.label.toString() + "\n" +
                "end" + endNo.toString() + ": \n";
        endNo += 1;
        program.add(instr);
        return null;
    }

    @Override
    public List<String> visit(Call call) {
        Integer size = call.args.size() * 4;
        String stackAlloc = " li t6, " + size.toString() + "\n" +
                "  sub sp, sp, t6\n";
        program.add(stackAlloc);
        Integer c = 0;
        for (Identifier identifier : call.args) {
            Integer offset = cStackRef.get(identifier.toString());
            String preamble = "  lw t6, " + offset.toString() + "(fp)\n" +
                    "  sw t6, " + c.toString() + "(sp) \n";
            c += 4;
            program.add(preamble);
        }
        program.add("jalr " + call.callee.toString() + "\n");
        program.add("mv " + call.lhs.toString() + ", a0");
        return null;
    }

    private String threeOp(String op, String rd, String rs1, String rs2) {
        return op + " " + rd + ", " + rs1 + ", " + rs2 + "\n";
    }
}
