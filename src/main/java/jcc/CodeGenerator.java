package jcc;

import static jcc.JccParser.*;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.ListIterator;
import java.util.Map;

import jcc.Code.Instruction;
import jcc.ast.BinOpNode;
import jcc.ast.BlockNode;
import jcc.ast.BreakNode;
import jcc.ast.ContinueNode;
import jcc.ast.ExprNode;
import jcc.ast.ExprStmtNode;
import jcc.ast.FuncCallNode;
import jcc.ast.FuncDefNode;
import jcc.ast.IfNode;
import jcc.ast.IntLiteralNode;
import jcc.ast.PrintfNode;
import jcc.ast.ProgramNode;
import jcc.ast.ReturnNode;
import jcc.ast.StrLiteralNode;
import jcc.ast.VarDefNode;
import jcc.ast.VarInitNode;
import jcc.ast.VarLetNode;
import jcc.ast.VarRefNode;
import jcc.ast.WhileNode;
import jcc.value.IntegerValue;
import jcc.value.PointerValue;
import lombok.Getter;

@Getter
public class CodeGenerator implements NodeVisitor<Void, Void> {

    private final List<Code> codes = new ArrayList<>();
    private final Map<String, FuncDefinition> funcDefs = new HashMap<>();
    private final ConstTable cTbl = new ConstTable();
    
    public void generate(ProgramNode n) {
        n.getFuncDefs().forEach(f -> funcDefs.put(f.getFname(), new FuncDefinition(f, IntegerValue.of(0))));
        n.getFuncDefs().forEach(f -> f.accept(this));
    }
    
    void debugCode() {
        codes.stream()
            .map(c -> String.format("%s\t%s", c.getInst().name(),
                    c.getOperand() == null ? "" : c.getOperand().toString()))
            .forEach(System.out::println);
    }
    
    FunctionScope fScope;
    @Override
    public Void visit(FuncDefNode n) {
        fScope = new FunctionScope(); // Initialize func scope
        
        FuncDefinition fd = funcDefs.get(n.getFname());
        fd.getFuncAddr().setVal(codes.size());; // Set idx of code as FuncAddr
        codes.add(new Code(Instruction.ENTRY, fd.getFuncAddr()));
        
        IntegerValue lvarCnt = IntegerValue.of(0);
        codes.add(new Code(Instruction.FRAME, lvarCnt));
        
        // Process func args (Register args to func scope)
        fd.getParams().forEach(p -> fScope.addArg(p.getType(), p.getPname()));
        
        // Process func body 
        n.getBlock().accept(this);
        
        lvarCnt.setVal(fScope.getLvarIdx()); // Set total count of lvar finally
        return null;
    }

    @Override
    public Void visit(BlockNode n) {
        fScope.pushScope();
        
        n.getStmts().forEach(s -> s.accept(this));
        
        fScope.popScope();
        return null;
    }

    @Override
    public Void visit(ReturnNode n) {
        if (n.getExpr() != null) {
            n.getExpr().accept(this);
            codes.add(new Code(Instruction.RET, IntegerValue.of(0))); // With retval
        } else {
            codes.add(new Code(Instruction.RET, IntegerValue.of(1))); // No retval
        }
        return null;
    }

    @Override
    public Void visit(IntLiteralNode n) {
        codes.add(new Code(Instruction.PUSH, IntegerValue.of(n.getVal())));
        return null;
    }

    @Override
    public Void visit(BinOpNode n) {
        n.getLeft().accept(this);
        n.getRight().accept(this);
        switch (n.getOpType()) {
        case ADD:
            codes.add(new Code(Instruction.ADD, IntegerValue.of(0)));
            break;
        case SUB:
            codes.add(new Code(Instruction.SUB, IntegerValue.of(0)));
            break;
        case MUL:
            codes.add(new Code(Instruction.MUL, IntegerValue.of(0)));
            break;
        case DIV:
            codes.add(new Code(Instruction.DIV, IntegerValue.of(0)));
            break;
        case MOD:
            codes.add(new Code(Instruction.MOD, IntegerValue.of(0)));
            break;
        case LSHIFT:
        case RSHIFT:
            codes.add(new Code(Instruction.BSHIFT, IntegerValue.of(n.getOpType())));
            break;
        case EQEQ:
        case NOTEQ:
        case GT:
        case LT:
        case GTE:
        case LTE:
            codes.add(new Code(Instruction.CMP, IntegerValue.of(n.getOpType())));
            break;
        default:
            throw new IllegalArgumentException(String.valueOf(n.getOpType()));
        }
        return null;
    }

    @Override
    public Void visit(VarRefNode n) {
        LvarDefinition var = fScope.getVar(n.getVname());
        if (var.isArg()) {
            codes.add(new Code(Instruction.LOADA, IntegerValue.of(var.getIdx())));
        } else {
            codes.add(new Code(Instruction.LOADL, IntegerValue.of(var.getIdx())));
        }
        return null;
    }

    @Override
    public Void visit(VarLetNode n) {
        n.getExpr().accept(this);
        LvarDefinition var = fScope.getVar(n.getVar().getVname());
        if (var.isArg()) {
            codes.add(new Code(Instruction.STOREA, IntegerValue.of(var.getIdx())));
        } else {
            codes.add(new Code(Instruction.STOREL, IntegerValue.of(var.getIdx())));
        }
        return null;
    }

    @Override
    public Void visit(VarDefNode n) {
        fScope.addLvar(n.getType(), n.getVname());
        return null;
    }
    
    @Override
    public Void visit(VarInitNode n) {
        LvarDefinition var = fScope.addLvar(n.getLvar().getType(), n.getLvar().getVname());
        n.getExpr().accept(this);
        codes.add(new Code(Instruction.STOREL, IntegerValue.of(var.getIdx())));
        return null;
    }

    @Override
    public Void visit(FuncCallNode n) {
        FuncDefinition fd = funcDefs.get(n.getFname());
        if (fd.getParams().size() != n.getArgs().size()) {
            throw new RuntimeException(String.format("Inconsistent arg counts '%s' for '%s'. "
                    + "Expected: %s", n.getArgs().size(), n.getFname(), fd.getParams().size()));
        }
        // Push args
        for (ListIterator<ExprNode> it = n.getArgs().listIterator(n.getArgs().size());
                it.hasPrevious();) {
            it.previous().accept(this);
        }
        codes.add(new Code(Instruction.CALL, fd.getFuncAddr()));
        codes.add(new Code(Instruction.POPR, IntegerValue.of(n.getArgs().size())));
        return null;
    }

    @Override
    public Void visit(ExprStmtNode n) {
        n.getExpr().accept(this);
        return null;
    }

    @Override
    public Void visit(IfNode n) {
        IntegerValue elseLbl = IntegerValue.of(0);
        IntegerValue fiLbl = IntegerValue.of(0);
        
        n.getCond().accept(this);
        codes.add(new Code(Instruction.JZ, elseLbl));
        n.getThenBody().accept(this);
        
        if (n.getElseBody() == null) {
            codes.add(new Code(Instruction.LABEL, elseLbl));
            elseLbl.setVal(codes.size() - 1);
        } else {
            codes.add(new Code(Instruction.JMP, fiLbl));
            codes.add(new Code(Instruction.LABEL, elseLbl));
            elseLbl.setVal(codes.size() - 1);
            n.getElseBody().accept(this);
            codes.add(new Code(Instruction.LABEL, fiLbl));
            fiLbl.setVal(codes.size() - 1);
        }
        return null;
    }

    @Override
    public Void visit(WhileNode n) {
        IntegerValue entLbl = IntegerValue.of(0);
        IntegerValue exitLbl = IntegerValue.of(0);
        
        codes.add(new Code(Instruction.LABEL, entLbl));
        entLbl.setVal(codes.size() - 1);
        n.getCond().accept(this);
        codes.add(new Code(Instruction.JZ, exitLbl));
        
        fScope.pushContinue(entLbl);
        fScope.pushBreak(exitLbl);
        n.getBody().accept(this);
        fScope.popBreak();
        fScope.popContinue();
        
        codes.add(new Code(Instruction.JMP, entLbl));
        codes.add(new Code(Instruction.LABEL, exitLbl));
        exitLbl.setVal(codes.size() - 1);
        return null;
    }

    @Override
    public Void visit(BreakNode n) {
        codes.add(new Code(Instruction.JMP, fScope.getBreakPoint()));
        return null;
    }

    @Override
    public Void visit(ContinueNode n) {
        codes.add(new Code(Instruction.JMP, fScope.getContinuePoint()));
        return null;
    }

    @Override
    public Void visit(PrintfNode n) {
        n.getFmtStr().accept(this);
        for (ListIterator<ExprNode> it = n.getArgs().listIterator(n.getArgs().size());
                it.hasPrevious();) {
            it.previous().accept(this);
        }
        codes.add(new Code(Instruction.PRINTF, IntegerValue.of(n.getArgs().size())));
        return null;
    }

    @Override
    public Void visit(StrLiteralNode n) {
        cTbl.add(n.getVal());
        PointerValue<Character> p = new PointerValue<>(0, StrUtils.strToCharacterArray(n.getVal()));
        codes.add(new Code(Instruction.PUSHP, p));
        return null;
    }

}
