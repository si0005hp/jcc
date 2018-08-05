package jcc;

import static jcc.JccParser.ADD;
import static jcc.JccParser.DIV;
import static jcc.JccParser.MUL;
import static jcc.JccParser.SUB;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.ListIterator;
import java.util.Map;

import jcc.Code.Instruction;
import jcc.ast.BinOpNode;
import jcc.ast.BlockNode;
import jcc.ast.ExprNode;
import jcc.ast.FuncCallNode;
import jcc.ast.FuncDefNode;
import jcc.ast.IntLiteralNode;
import jcc.ast.ProgramNode;
import jcc.ast.ReturnNode;
import jcc.ast.VarDefNode;
import jcc.ast.VarInitNode;
import jcc.ast.VarLetNode;
import jcc.ast.VarRefNode;
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
            .map(c -> String.format("%s\t%s", c.getInst().name(), c.getOperand().getVal()))
            .forEach(System.out::println);
    }
    
    FunctionScope fScope;
    @Override
    public Void visit(FuncDefNode n) {
        fScope = new FunctionScope();
        
        FuncDefinition fd = funcDefs.get(n.getFname());
        fd.getFuncAddr().setVal(codes.size()); // Set idx of code as FuncAddr 
        
        codes.add(new Code(Instruction.ENTRY, fd.getFuncAddr()));
        
        n.getBlock().accept(this);
        return null;
    }

    @Override
    public Void visit(BlockNode n) {
        fScope.addScope();
        
        long lvarCnt = n.getStmts().stream()
                .filter(s -> s instanceof VarDefNode || s instanceof VarInitNode)
                .count();
        codes.add(new Code(Instruction.FRAME, MutableLong.of(lvarCnt)));    
        
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
        default:
            throw new IllegalArgumentException(String.valueOf(n.getOpType()));
        }
        return null;
    }

    @Override
    public Void visit(VarDefNode n) {
        fScope.addVar(n.getType(), n.getVname());
        return null;
    }
    
    @Override
    public Void visit(VarRefNode n) {
        codes.add(new Code(Instruction.LOADL, 
                MutableLong.of(fScope.getVar(n.getVname()).getFIdx())));
        return null;
    }

    @Override
    public Void visit(VarLetNode n) {
        n.getExpr().accept(this);
        codes.add(new Code(Instruction.STOREL, 
                MutableLong.of(fScope.getVar(n.getVar().getVname()).getFIdx())));
        return null;
    }

    @Override
    public Void visit(VarInitNode n) {
        LvarDefinition lvar = fScope.addVar(n.getLvar().getType(), n.getLvar().getVname());
        n.getExpr().accept(this);
        codes.add(new Code(Instruction.STOREL, MutableLong.of(lvar.getFIdx())));
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
    
}
