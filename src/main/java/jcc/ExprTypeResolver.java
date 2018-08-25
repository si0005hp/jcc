package jcc;

import java.util.HashMap;
import java.util.Map;

import jcc.ast.AddressNode;
import jcc.ast.BinOpNode;
import jcc.ast.BlockNode;
import jcc.ast.FuncCallNode;
import jcc.ast.FuncDefNode;
import jcc.ast.VarDefNode;
import jcc.ast.VarRefNode;
import jcc.type.ArrayType;
import jcc.type.IntegerType;
import jcc.type.PointerType;
import jcc.type.Type;
import lombok.Getter;

@Getter
public class ExprTypeResolver extends AbstractVisitor {

    private final Map<String, FuncDefNode> fns = new HashMap<>();
    
    FunctionScope fScope;
    @Override
    public Void visit(FuncDefNode n) {
        fScope = new FunctionScope(n);
        fns.put(n.getFname(), n);
        
        super.visit(n);
        return null;
    }
    
    @Override
    public Void visit(BlockNode n) {
        fScope.pushScope();
        super.visit(n);
        fScope.popScope();
        return null;
    }
    
    @Override
    public Void visit(VarDefNode n) {
        fScope.addVar(n);
        return null;
    }
    
    @Override
    public Void visit(VarRefNode n) {
        LvarDefinition var = fScope.getVar(n.getVname());
        if (var.getType() instanceof ArrayType) {
            // int a[2]; int *p = a; -> Ref of 'a' should be pointer
            n.setType(PointerType.of(var.getType().baseType()));
        } else {
            n.setType(var.getType());            
        }
        return null;
    }
    
    @Override
    public Void visit(AddressNode n) {
        super.visit(n);
        Type vType = n.getVar().type();
        if (vType instanceof ArrayType) {
            n.setType(vType);
        } else {
            n.setType(PointerType.of(vType));
        }
        return null;
    }
    
    @Override
    public Void visit(FuncCallNode n) {
        super.visit(n);
        if (fns.containsKey(n.getFname())) {
            n.setType(fns.get(n.getFname()).getRetvalType());    
        } else {
            // Temporarily int since it can't capture ext func so far
            n.setType(IntegerType.of(CType.INT));
        }
        return null;
    }
    
    @Override
    public Void visit(BinOpNode n) {
        super.visit(n);
        if (n.getLeft().type() instanceof PointerType 
                && n.getRight().type() instanceof PointerType) {
            n.setType(IntegerType.of(CType.LONG));
        } else if (n.getLeft().type() instanceof PointerType) {
            n.setType(n.getLeft().type());
        } else if (n.getRight().type() instanceof PointerType) {
            n.setType(n.getRight().type());
        } else {
            n.setType(implicitIntCast((IntegerType) n.getLeft().type(),
                    (IntegerType) n.getRight().type()));
        }
        return null;
    }
    
    private IntegerType implicitIntCast(IntegerType l, IntegerType r) {
        if (l.getBaseType() == r.getBaseType()) {
            return IntegerType.of(l.getBaseType());
        } else if (l.getSize() > r.getSize()) {
            return IntegerType.of(l.getBaseType());
        } else {
            return IntegerType.of(r.getBaseType());
        }
    }
    
}
