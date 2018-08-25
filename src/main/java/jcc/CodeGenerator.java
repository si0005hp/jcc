package jcc;

import static jcc.JccParser.*;

import java.util.Arrays;
import java.util.List;

import jcc.ast.AddressNode;
import jcc.ast.ArrLiteralNode;
import jcc.ast.BinOpNode;
import jcc.ast.BlockNode;
import jcc.ast.BreakNode;
import jcc.ast.ContinueNode;
import jcc.ast.DereferNode;
import jcc.ast.ExprNode;
import jcc.ast.ExprStmtNode;
import jcc.ast.ForNode;
import jcc.ast.FuncCallNode;
import jcc.ast.FuncDefNode;
import jcc.ast.IfNode;
import jcc.ast.IntLiteralNode;
import jcc.ast.ProgramNode;
import jcc.ast.ReturnNode;
import jcc.ast.StrLiteralNode;
import jcc.ast.VarDefNode;
import jcc.ast.VarInitNode;
import jcc.ast.AssignNode;
import jcc.ast.VarRefNode;
import jcc.ast.WhileNode;
import jcc.type.ArrayType;
import jcc.type.IntegerType;
import jcc.type.PointerType;
import jcc.type.VoidType;
import lombok.Getter;

@Getter
public class CodeGenerator implements NodeVisitor<Void, Void> {

    private static final List<String> ARG_REGS = Arrays.asList("rdi", "rsi", "rdx", "rcx", "r8", "r9");
    
    private final Asm asm = new Asm();
    private final ConstantTable constTbl;
    
    private int jLblIdx = 0;

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
        fScope = new FunctionScope(n);

        /* prologue */
        asm.gen(".global %s", n.getFname());
        asm.gen("%s:", n.getFname());
        asm.gent("push %%rbp");
        asm.gent("mov %%rsp, %%rbp");
        int iSum = n.getVars().stream().mapToInt(VarDefNode::getIdx).max().orElse(0);
        if (iSum > 0) {
            asm.gent("sub $%s, %%rsp", iSum);    
        }
        
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
        IntegerType t = (IntegerType) n.getType();
        switch (t.getBaseType()) {
        case INT:
            asm.gent("mov $%d, %%eax", n.getVal());
            break;
        case CHAR:
            asm.gent("mov $%d, %%rax", n.getVal());
            break;
        default:
            throw new IllegalArgumentException(t.getBaseType().name());
        }
        return null;
    }

    @Override
    public Void visit(BinOpNode n) {
        if (n.getType() instanceof PointerType) {
            binOpPtrArith(n);
            return null;
        }
        
        String op = null;
        switch (n.getOpType()) {
        case GT: binOpCmp("setg", n); return null;
        case LT: binOpCmp("setl", n); return null;
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
    
    private void binOpPtrArith(BinOpNode n) {
        ExprNode ptr; ExprNode integer;
        if (n.getLeft().type() instanceof PointerType) {
            ptr = n.getLeft(); integer = n.getRight(); 
        } else {
            ptr = n.getRight(); integer = n.getLeft();
        }
        ptr.accept(this);
        asm.gent("push %%rax");
        integer.accept(this);
        int ptrBaseSze = ptr.type().baseType().getSize();
        if (ptrBaseSze > 1) {
            asm.gent("imul $%s, %%rax", ptrBaseSze);
        }
        asm.gent("mov %%rax, %%rcx");
        asm.gent("pop %%rax");
        asm.gent("add %%rcx, %%rax");
    }
    
    private void binOpCmp(String inst, BinOpNode n) {
        n.getLeft().accept(this);
        asm.gent("push %%rax");
        n.getRight().accept(this);
        asm.gent("pop %%rcx");
        asm.gent("cmp %%rax, %%rcx");
        asm.gent("%s %%al", inst);
        asm.gent("movzb %%al, %%eax");
    }
    
    @Override
    public Void visit(VarDefNode n) {
        fScope.addVar(n);
        return null;
    }
    
    @Override
    public Void visit(AssignNode n) {
        assign(n.getVar(), n.getExpr());
        return null;
    }
    
    private void assign(ExprNode l, ExprNode v) {
        if (l instanceof VarRefNode) {
             assignVar(((VarRefNode)l).getVname(), v);
        } else if (l instanceof DereferNode) {
            assignDerefer((DereferNode)l, v);
        } else {
            throw new IllegalArgumentException(l.getClass().getName());
        }
    }
    
    private void assignVar(String vname, ExprNode val) {
        val.accept(this);
        LvarDefinition var = fScope.getVar(vname);
        if (var.isArg()) {
            asm.gent("mov %%rax, %%%s", ARG_REGS.get(var.getIdx() - 1));
        } else {
            asm.gent("mov %%%s, %s(%%rbp)", axBySize(var.getType().getSize()), var.getIdx());
        }
    }
    
    private void assignDerefer(DereferNode l, ExprNode val) {
        l.getVar().accept(this);
        asm.gent("push %%rax");
        val.accept(this);
        asm.gent("pop %%rcx");
        asm.gent("mov %%%s, (%%rcx)", axBySize(l.type().getSize()));
    }
    
    @Override
    public Void visit(VarInitNode n) {
        n.getLvar().accept(this);
        if (n.getLvar().getType() instanceof ArrayType) {
            initArr(n.getLvar(), (ArrLiteralNode) n.getExpr());
        } else {
            assignVar(n.getLvar().getVname(), n.getExpr());            
        }
        return null;
    }
    
    private void initArr(VarDefNode v, ArrLiteralNode n) {
        ArrayType t = (ArrayType) v.getType();
        for (int i = 0; i < n.getElems().size(); i++) {
            n.getElems().get(i).accept(this);
            asm.gent("mov %%%s, %s(%%rbp)", 
                    axBySize(t.getBaseType().getSize()),
                    -(v.getIdx() - i * t.getBaseType().getSize()));
        }
    }
    
    @Override
    public Void visit(ArrLiteralNode n) {
        return null; // Nothing to do
    }

    @Override
    public Void visit(VarRefNode n) {
        LvarDefinition var = fScope.getVar(n.getVname());
        if (var.getType() instanceof ArrayType) {
            refAddres(var);
        } else {
            if (var.isArg()) {
                asm.gent("mov %%%s, %%rax", ARG_REGS.get(var.getIdx() - 1));
            } else {
                String ax = axBySize(var.getType().getSize());
                if (ax.equals("al")) {
                    asm.gent("mov $0, %%eax", var.getIdx(), ax);
                }
                asm.gent("mov %s(%%rbp), %%%s", var.getIdx(), ax);
            }
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
        refAddres(var);
        return null;
    }
    
    private void refAddres(LvarDefinition var) {
        if (var.isArg()) {
            asm.gent("lea %%%s, %%rax", ARG_REGS.get(var.getIdx() - 1));
        } else {
            asm.gent("lea %s(%%rbp), %%rax", var.getIdx());
        }
    }

    @Override
    public Void visit(DereferNode n) {
        n.getVar().accept(this);
        asm.gent("mov (%%rax), %%rax");
        return null;
    }

    @Override
    public Void visit(IfNode n) {
        n.getCond().accept(this);
        String els = makeJLbl();
        asm.gent("test %%rax, %%rax");
        asm.gent("je %s", els);
        n.getThenBody().accept(this);
        if (n.getElseBody() != null) {
            String end = makeJLbl();
            asm.gent("jmp %s", end);
            asm.gen("%s:", els);
            n.getElseBody().accept(this);
            asm.gen("%s:", end);
        } else {
            asm.gen("%s:", els);
        }
        return null;
    }
    
    private String makeJLbl() {
        return String.format(".L%d", jLblIdx++);
    }
    
    @Override
    public Void visit(ForNode n) {
        if (n.getInit() != null) {
            n.getInit().accept(this);
        }
        String begin = makeJLbl();
        String end = makeJLbl();
        asm.gen("%s:", begin);
        if (n.getCond() != null) {
            n.getCond().accept(this);
            asm.gent("test %%rax, %%rax");
            asm.gent("je %s", end);
        }
        n.getBody().accept(this);
        if (n.getStep() != null) {
            n.getStep().accept(this);
        }
        asm.gent("jmp %s", begin);
        asm.gen("%s:", end);
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
