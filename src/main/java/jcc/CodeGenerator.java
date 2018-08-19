package jcc;

import static jcc.JccParser.ADD;
import static jcc.JccParser.DIV;
import static jcc.JccParser.MUL;
import static jcc.JccParser.SUB;

import java.util.Arrays;
import java.util.List;

import jcc.ast.AddressNode;
import jcc.ast.BinOpNode;
import jcc.ast.BlockNode;
import jcc.ast.BreakNode;
import jcc.ast.ContinueNode;
import jcc.ast.DereferNode;
import jcc.ast.ExprNode;
import jcc.ast.ExprStmtNode;
import jcc.ast.FuncCallNode;
import jcc.ast.FuncDefNode;
import jcc.ast.IfNode;
import jcc.ast.IntLiteralNode;
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
    private final ConstantTable constTbl;

    public CodeGenerator(NodePreprocessor pp) {
        this.constTbl = pp.getConstTbl();
    }
    
    @Override
    public void visit(ProgramNode n) {
        genDataSection();
        genTextSection(n);
    }
    
    private void genDataSection() {
        if (constTbl.getStrLblIdx() > 0) {
            asm.gen(".data");
            constTbl.getStrLiterals().forEach((s, lbl) -> {
                asm.gen("%s:", lbl);
                asm.gent(".string \"%s\"", s);
            });
        }
    }
    
    private void genTextSection(ProgramNode n) {
        if (!n.getFuncDefs().isEmpty()) {
            asm.gen(".text");
            n.getFuncDefs().forEach(f -> f.accept(this));
        }
    }
    
    private String axBySize(int size) {
        switch (size) {
        case 1: return "al";
        case 4: return "eax";
        case 8: return "rax";
        default:
            throw new IllegalArgumentException(String.valueOf(size));
        }
    }
    
    FunctionScope fScope;
    @Override
    public Void visit(FuncDefNode n) {
        fScope = new FunctionScope();
        // Register params to scope
        n.getParams().forEach(p -> fScope.addArg(p.getType(), p.getPname()));

        /* prologue */
        asm.gen(".global %s", n.getFname());
        asm.gen("%s:", n.getFname());
        asm.gent("push %%rbp");
        asm.gent("mov %%rsp, %%rbp");
        asm.gent("sub $%s, %%rsp", 8 * n.getLvarCnt());
        
        /* funcBody */
        n.getBlock().accept(this);
        
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
        if (n.getExpr() != null) {
            n.getExpr().accept(this);            
        }
        asm.gent("leave");
        asm.gent("ret");
        return null;
    }
    
    @Override
    public Void visit(IntLiteralNode n) {
        switch (n.getCType()) {
        case INT:
            asm.gent("mov $%d, %%eax", n.getVal());
            break;
        case CHAR:
            asm.gent("mov $%d, %%rax", n.getVal());
            break;
        default:
            throw new IllegalArgumentException(n.getCType().name());
        }
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
        varLet(n.getVar().getVname(), n.getExpr());
        return null;
    }
    
    @Override
    public Void visit(VarInitNode n) {
        fScope.addLvar(n.getLvar().getType(), n.getLvar().getVname());
        varLet(n.getLvar().getVname(), n.getExpr());
        return null;
    }
    
    private void varLet(String vname, ExprNode val) {
        val.accept(this);
        LvarDefinition var = fScope.getVar(vname);
        if (var.isArg()) {
            asm.gent("mov %%rax, %%%s", ARG_REGS.get(var.getIdx() - 1));
        } else {
            String ax = axBySize(var.getType().getSize());
            asm.gent("mov %%%s, %s(%%rbp)", ax, var.getIdx());
        }
    }
    
    @Override
    public Void visit(VarRefNode n) {
        LvarDefinition var = fScope.getVar(n.getVname());
        if (var.isArg()) {
            asm.gent("mov %%%s, %%rax", ARG_REGS.get(var.getIdx() - 1));
        } else {
            String ax = axBySize(var.getType().getSize());
            if (ax.equals("al")) {
                asm.gent("mov $0, %%eax", var.getIdx(), ax);
            }
            asm.gent("mov %s(%%rbp), %%%s", var.getIdx(), ax);
        }
        return null;
    }

    @Override
    public Void visit(FuncCallNode n) {
        // process args
        for (int i = 0; i < n.getArgs().size(); i++) {
            n.getArgs().get(i).accept(this);
            asm.gent("push %%rax");
        }
        for (int i = n.getArgs().size() - 1; i >= 0; i--) {
            asm.gent("pop %%%s", ARG_REGS.get(i));
        }
        // call func
        asm.gent("mov $0, %%eax");
        asm.gent("call %s", n.getFname());
        return null;
    }

    @Override
    public Void visit(ExprStmtNode n) {
        n.getExpr().accept(this);
        return null;
    }
    
    @Override
    public Void visit(StrLiteralNode n) {
        asm.gent("lea %s(%%rip), %%rax", n.getLbl());
        return null;
    }

    @Override
    public Void visit(AddressNode n) {
        LvarDefinition var = fScope.getVar(n.getVar().getVname());
        if (var.isArg()) {
            // TODO
        } else {
            asm.gent("lea %s(%%rbp), %%rax", var.getIdx());
        }
        return null;
    }

    @Override
    public Void visit(DereferNode n) {
        n.getVar().accept(this);
        asm.gent("mov (%%rax), %%rax");
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

}
