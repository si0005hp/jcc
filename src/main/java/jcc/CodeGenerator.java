package jcc;

import static jcc.JccParser.ADD;
import static jcc.JccParser.DIV;
import static jcc.JccParser.MUL;
import static jcc.JccParser.SUB;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import jcc.ast.BinOpNode;
import jcc.ast.BlockNode;
import jcc.ast.FuncDefNode;
import jcc.ast.IntLiteralNode;
import jcc.ast.ProgramNode;
import jcc.ast.ReturnNode;
import jcc.ast.VarDefNode;
import jcc.ast.VarInitNode;
import jcc.ast.VarLetNode;
import jcc.ast.VarRefNode;
import jcc.code.Code;
import jcc.code.Code.Instruction;
import lombok.Getter;

@Getter
public class CodeGenerator implements NodeVisitor<Void, Void> {

    private final List<Code> codes = new ArrayList<>();
    private final Map<String, FuncDefinition> funcDefs = new HashMap<>();
    
    public void generate(ProgramNode n) {
        n.getFuncDefs().forEach(f -> f.accept(this));
    }
    
    void debugCode() {
        codes.stream()
            .map(c -> String.format("%s\t%s", c.getInst().name(), c.getOperand()))
            .forEach(System.out::println);;
    }
    
    Map<String, LvarDefinition> env;
    @Override
    public Void visit(BlockNode n) {
        env = new HashMap<>();
        
        long lvarCnt = n.getStmts().stream()
                .filter(s -> s instanceof VarDefNode || s instanceof VarInitNode)
                .count();
        codes.add(new Code(Instruction.FRAME, lvarCnt));    
        
        n.getStmts().forEach(s -> s.accept(this));
        
        env = null;
        return null;
    }

    @Override
    public Void visit(FuncDefNode n) {
        Code e = new Code(Instruction.ENTRY);
        codes.add(e);
        
        FuncDefinition fd = new FuncDefinition(n, codes.indexOf(e));
        funcDefs.put(n.getFname(), fd);
        
        n.getBlock().accept(this);
        return null;
    }

    @Override
    public Void visit(ReturnNode n) {
        if (n.getExpr() != null) {
            n.getExpr().accept(this);
            codes.add(new Code(Instruction.RET, 0)); // With retval
        } else {
            codes.add(new Code(Instruction.RET, 1)); // No retval
        }
        return null;
    }

    @Override
    public Void visit(IntLiteralNode n) {
        codes.add(new Code(Instruction.PUSH, n.getVal()));
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
        env.put(n.getVname(), new LvarDefinition(n, env.size() + 1));
        return null;
    }
    
    @Override
    public Void visit(VarRefNode n) {
        LvarDefinition lvar = env.get(n.getVname());
        codes.add(new Code(Instruction.LOADL, lvar.getFIdx()));
        return null;
    }

    @Override
    public Void visit(VarLetNode n) {
        n.getExpr().accept(this);
        LvarDefinition lvar = env.get(n.getVar().getVname());
        codes.add(new Code(Instruction.STOREL, lvar.getFIdx()));
        return null;
    }

    @Override
    public Void visit(VarInitNode n) {
        LvarDefinition lvar = new LvarDefinition(n.getLvar(), env.size() + 1);
        env.put(lvar.getVname(), lvar);
        n.getExpr().accept(this);
        codes.add(new Code(Instruction.STOREL, lvar.getFIdx()));
        return null;
    }
    
}
