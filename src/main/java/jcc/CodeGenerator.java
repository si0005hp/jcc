package jcc;

import static jcc.JccParser.ADD;
import static jcc.JccParser.DIV;
import static jcc.JccParser.EQEQ;
import static jcc.JccParser.GT;
import static jcc.JccParser.GTE;
import static jcc.JccParser.LT;
import static jcc.JccParser.LTE;
import static jcc.JccParser.MUL;
import static jcc.JccParser.NOTEQ;
import static jcc.JccParser.SUB;

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
import jcc.ast.ProgramNode;
import jcc.ast.ReturnNode;
import jcc.ast.VarDefNode;
import jcc.ast.VarInitNode;
import jcc.ast.VarLetNode;
import jcc.ast.VarRefNode;
import jcc.ast.WhileNode;
import lombok.Getter;

@Getter
public class CodeGenerator implements NodeVisitor<Void, Void> {

    private final List<Code> codes = new ArrayList<>();
    private final Map<String, FuncDefinition> funcDefs = new HashMap<>();
    
    public void generate(ProgramNode n) {
        n.getFuncDefs().forEach(f -> funcDefs.put(f.getFname(), new FuncDefinition(f, MutableLong.of(0))));
        n.getFuncDefs().forEach(f -> f.accept(this));
    }
    
    void debugCode() {
        codes.stream()
            .map(c -> String.format("%s\t%s", c.getInst().name(),
                    c.getOperand() == null ? "" : c.getOperand().getVal()))
            .forEach(System.out::println);
    }
    
    FunctionScope fScope;
    @Override
    public Void visit(FuncDefNode n) {
        fScope = new FunctionScope(); // Initialize func scope
        
        FuncDefinition fd = funcDefs.get(n.getFname());
        fd.getFuncAddr().setVal(codes.size()); // Set idx of code as FuncAddr
        codes.add(new Code(Instruction.ENTRY, fd.getFuncAddr()));
        
        MutableLong lvarCnt = MutableLong.of(0);
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
        fScope.addScope();
        
        n.getStmts().forEach(s -> s.accept(this));
        
        fScope.removeScope();
        return null;
    }

    @Override
    public Void visit(ReturnNode n) {
        if (n.getExpr() != null) {
            n.getExpr().accept(this);
            codes.add(new Code(Instruction.RET, MutableLong.of(0))); // With retval
        } else {
            codes.add(new Code(Instruction.RET, MutableLong.of(1))); // No retval
        }
        return null;
    }

    @Override
    public Void visit(IntLiteralNode n) {
        codes.add(new Code(Instruction.PUSH, MutableLong.of(n.getVal())));
        return null;
    }

    @Override
    public Void visit(BinOpNode n) {
        n.getLeft().accept(this);
        n.getRight().accept(this);
        switch (n.getOpType()) {
        case ADD:
            codes.add(new Code(Instruction.ADD));
            break;
        case SUB:
            codes.add(new Code(Instruction.SUB));
            break;
        case MUL:
            codes.add(new Code(Instruction.MUL));
            break;
        case DIV:
            codes.add(new Code(Instruction.DIV));
            break;
        case EQEQ:
        case NOTEQ:
        case GT:
        case LT:
        case GTE:
        case LTE:
        default:
            throw new IllegalArgumentException(String.valueOf(n.getOpType()));
        }
        return null;
    }

    @Override
    public Void visit(VarRefNode n) {
        LvarDefinition var = fScope.getVar(n.getVname());
        if (var.isArg()) {
            codes.add(new Code(Instruction.LOADA, MutableLong.of(var.getIdx())));
        } else {
            codes.add(new Code(Instruction.LOADL, MutableLong.of(var.getIdx())));
        }
        return null;
    }

    @Override
    public Void visit(VarLetNode n) {
        n.getExpr().accept(this);
        LvarDefinition var = fScope.getVar(n.getVar().getVname());
        if (var.isArg()) {
            codes.add(new Code(Instruction.STOREA, MutableLong.of(var.getIdx())));
        } else {
            codes.add(new Code(Instruction.STOREL, MutableLong.of(var.getIdx())));
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
        codes.add(new Code(Instruction.STOREL, MutableLong.of(var.getIdx())));
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
        codes.add(new Code(Instruction.POPR, MutableLong.of(n.getArgs().size())));
        return null;
    }

    @Override
    public Void visit(ExprStmtNode n) {
        n.getExpr().accept(this);
        return null;
    }

    @Override
    public Void visit(IfNode n) {
        MutableLong elseAddr = MutableLong.of(0);
        MutableLong fiAddr = MutableLong.of(0);
        
        n.getCond().accept(this);
        codes.add(new Code(Instruction.JZ, elseAddr));
        n.getThenBody().accept(this);
        
        if (n.getElseBody() == null) {
            codes.add(new Code(Instruction.LABEL, elseAddr));
            elseAddr.setVal(codes.size() - 1);
        } else {
            codes.add(new Code(Instruction.JMP, fiAddr));
            codes.add(new Code(Instruction.LABEL, elseAddr));
            elseAddr.setVal(codes.size() - 1);
            n.getElseBody().accept(this);
            codes.add(new Code(Instruction.LABEL, fiAddr));
            fiAddr.setVal(codes.size() - 1);
        }
        return null;
    }

    @Override
    public Void visit(WhileNode n) {
        MutableLong entAddr = MutableLong.of(0);
        MutableLong exitAddr = MutableLong.of(0);
        
        codes.add(new Code(Instruction.LABEL, entAddr));
        n.getCond().accept(this);
        codes.add(new Code(Instruction.JZ, exitAddr));
        
        fScope.pushContinue(entAddr);
        fScope.pushBreak(exitAddr);
        n.getBody().accept(this);
        fScope.popBreak();
        fScope.popContinue();
        
        codes.add(new Code(Instruction.JMP, entAddr));
        codes.add(new Code(Instruction.LABEL, exitAddr));
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

}
