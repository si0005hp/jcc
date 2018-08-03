package jcc;

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
import jcc.code.Code;
import jcc.code.Code.Instruction;
import lombok.Getter;
import static jcc.JccParser.ADD;
import static jcc.JccParser.SUB;
import static jcc.JccParser.MUL;
import static jcc.JccParser.DIV;

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
    
    @Override
    public Void visit(BlockNode n) {
        n.getStmts().forEach(s -> s.accept(this));
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
    public Void visit(VarDefNode n) {
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
    
}
