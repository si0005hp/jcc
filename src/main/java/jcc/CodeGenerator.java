package jcc;

import static jcc.JccParser.ADD;
import static jcc.JccParser.DIV;
import static jcc.JccParser.MUL;
import static jcc.JccParser.SUB;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import jcc.ast.AddressNode;
import jcc.ast.BinOpNode;
import jcc.ast.BlockNode;
import jcc.ast.BreakNode;
import jcc.ast.ContinueNode;
import jcc.ast.DereferNode;
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
import jcc.type.VoidType;
import lombok.Getter;

@Getter
public class CodeGenerator implements NodeVisitor<Void, Void> {

    private static final List<String> ARG_REGS = Arrays.asList("rdi", "rsi", "rdx", "rcx", "r8", "r9");
    
    private final Asm asm = new Asm();
    private final Map<String, FuncDefinition> funcDefs = new HashMap<>();
    private final ConstTable cTbl = new ConstTable();

    public void generate(ProgramNode n) {
        if (!n.getFuncDefs().isEmpty()) {
            asm.gen(".text");
            n.getFuncDefs().forEach(f -> f.accept(this));
        }
    }
    
    FunctionScope fScope;
    @Override
    public Void visit(FuncDefNode n) {
        fScope = new FunctionScope();
        
        /* prologue */
        asm.gen(".global %s", n.getFname());
        asm.gen("%s:", n.getFname());
        asm.gent("push %%rbp");
        asm.gent("mov %%rsp, %%rbp");
        // params
        MutableNum bOffset = MutableNum.of(0);
        List<MutableNum> bpIdxs = new ArrayList<>();
        for (int i = 0; i < n.getParams().size(); i++) {
            bpIdxs.add(MutableNum.of(0));
            asm.gent("mov %%%s %s(%%rbp)", ARG_REGS.get(i), bpIdxs.get(i));
        }
        // Expand sp based on local vas
        MutableNum spWid = MutableNum.of(0);
        asm.gent("sub $%s, %%rsp", spWid);
        
        /* funcBody */
        n.getBlock().accept(this);
        // fix bpIdxs
        bOffset.setVal(-16 * fScope.getLvarIdx());
        for (int i = 0; i < bpIdxs.size(); i++) {
            bpIdxs.get(i).setVal(-4 * (i + 1) + bOffset.getVal());
        }
        // fix spWid
        spWid.setVal(4 * fScope.getLvarIdx());
        
        /* epilogue */
        if (n.getRetvalType() instanceof VoidType) {
            asm.gent("leave");
            asm.gent("ret");
        }
        
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
        n.getExpr().accept(this);
        asm.gent("leave");
        asm.gent("ret");
        return null;
    }
    
    @Override
    public Void visit(IntLiteralNode n) {
        asm.gent("mov $%d, %%eax", n.getVal());
        return null;
    }

    @Override
    public Void visit(BinOpNode n) {
        String op = null;
        switch (n.getOpType()) {
        case ADD: op = "add"; break;
        case SUB: op = "sub"; break;
        case MUL: op = "imul"; break;
        case DIV: break;
        default:
            throw new IllegalArgumentException(String.valueOf(n.getOpType()));
        }
        n.getRight().accept(this);
        asm.gent("push %%rax");
        n.getLeft().accept(this);
        if (n.getOpType() == DIV) {
            asm.gent("pop %%rcx");
            asm.gent("mov $0, %%edx");
            asm.gent("idiv %%rcx");
        } else {
            asm.gent("pop %%rcx");
            asm.gent("%s %%rcx, %%rax", op);
        }
        return null;
    }

    @Override
    public Void visit(VarDefNode n) {
        fScope.addLvar(n.getType(), n.getVname());
        return null;
    }
    
    @Override
    public Void visit(VarLetNode n) {
        n.getExpr().accept(this);
        int vIdx = fScope.getVar(n.getVar().getVname()).getIdx();  
        asm.gent("mov %%eax, %s(%%rbp)", -4 * vIdx);
        return null;
    }
    
    @Override
    public Void visit(VarRefNode n) {
        int vIdx = fScope.getVar(n.getVname()).getIdx();
        asm.gent("mov %s(%%rbp), %%eax", -4 * vIdx);
        return null;
    }

    @Override
    public Void visit(VarInitNode n) {
        LvarDefinition var = fScope.addLvar(n.getLvar().getType(), n.getLvar().getVname());
        n.getExpr().accept(this);
        asm.gent("mov %%eax, %s(%%rbp)", -4 * var.getIdx());
        return null;
    }
    
    @Override
    public Void visit(FuncCallNode n) {
        return null;
    }

    @Override
    public Void visit(StrLiteralNode n) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public Void visit(AddressNode n) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public Void visit(DereferNode n) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public Void visit(ExprStmtNode n) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public Void visit(IfNode n) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public Void visit(WhileNode n) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public Void visit(BreakNode n) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public Void visit(ContinueNode n) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public Void visit(PrintfNode n) {
        // TODO Auto-generated method stub
        return null;
    }

}
