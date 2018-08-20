package jcc;

import java.util.ArrayList;
import java.util.List;

import jcc.ast.FuncDefNode;
import jcc.ast.VarDefNode;
import jcc.ast.VarInitNode;

public class FuncVarAnalyzer extends AbstractVisitor {

    private List<VarDefNode> vars = new ArrayList<>();

    @Override
    public Void visit(FuncDefNode n) {
        super.visit(n);
        
        int idx = 0;
        for (VarDefNode v : vars) {
            idx += align8x(v.getType().getSize());
            v.setIdx(idx);
        }
        n.setVars(vars);
        
        vars = new ArrayList<>();
        return null;
    }

    @Override
    public Void visit(VarDefNode n) {
        vars.add(n);
        return null;
    }

    @Override
    public Void visit(VarInitNode n) {
        vars.add(n.getLvar());
        return null;
    }
    
    private int align8x(int n) {
        int r = n % 8;
        return (r == 0) ? n : n - r + 8;
    }
    
}
