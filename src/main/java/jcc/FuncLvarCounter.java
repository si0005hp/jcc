package jcc;

import jcc.ast.FuncDefNode;
import jcc.ast.VarDefNode;
import jcc.ast.VarInitNode;

public class FuncLvarCounter extends AbstractVisitor {
    
    private int lvarCnt = 0;
    
    @Override
    public Void visit(FuncDefNode n) {
        super.visit(n);
        n.setLvarCnt(lvarCnt);
        lvarCnt = 0;
        return null;
    }
    
    @Override
    public Void visit(VarDefNode n) {
        lvarCnt++;
        return null;
    }
    
    @Override
    public Void visit(VarInitNode n) {
        lvarCnt++;
        return null;
    }
}

