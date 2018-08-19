package jcc;

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
import jcc.ast.ProgramNode;
import jcc.ast.ReturnNode;
import jcc.ast.StrLiteralNode;
import jcc.ast.VarDefNode;
import jcc.ast.VarInitNode;
import jcc.ast.VarLetNode;
import jcc.ast.VarRefNode;
import jcc.ast.WhileNode;

public abstract class AbstractVisitor implements NodeVisitor<Void, Void> {

    @Override
    public void visit(ProgramNode n) {
        n.getFuncDefs().forEach(f -> f.accept(this));
    }
    
    @Override
    public Void visit(IntLiteralNode n) {
        return null;
    }

    @Override
    public Void visit(BinOpNode n) {
        n.getLeft().accept(this);
        n.getRight().accept(this);
        return null;
    }

    @Override
    public Void visit(VarRefNode n) {
        return null;
    }

    @Override
    public Void visit(FuncCallNode n) {
        n.getArgs().forEach(e -> e.accept(this));
        return null;
    }

    @Override
    public Void visit(StrLiteralNode n) {
        return null;
    }

    @Override
    public Void visit(AddressNode n) {
        n.getVar().accept(this);
        return null;
    }

    @Override
    public Void visit(DereferNode n) {
        n.getVar().accept(this);
        return null;
    }

    @Override
    public Void visit(BlockNode n) {
        n.getStmts().forEach(s -> s.accept(this));
        return null;
    }

    @Override
    public Void visit(FuncDefNode n) {
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
        }
        return null;
    }

    @Override
    public Void visit(VarLetNode n) {
        n.getExpr().accept(this);
        return null;
    }

    @Override
    public Void visit(VarInitNode n) {
        n.getLvar().accept(this);
        n.getExpr().accept(this);
        return null;
    }

    @Override
    public Void visit(ExprStmtNode n) {
        n.getExpr().accept(this);
        return null;
    }

    @Override
    public Void visit(IfNode n) {
        n.getCond().accept(this);
        n.getThenBody().accept(this);
        if (n.getElseBody() != null) {
            n.getElseBody().accept(this);
        }
        return null;
    }

    @Override
    public Void visit(WhileNode n) {
        n.getCond().accept(this);
        n.getBody().accept(this);
        return null;
    }

    @Override
    public Void visit(BreakNode n) {
        return null;
    }

    @Override
    public Void visit(ContinueNode n) {
        return null;
    }

}
