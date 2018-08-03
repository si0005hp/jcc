package jcc;

import jcc.ast.BinOpNode;
import jcc.ast.BlockNode;
import jcc.ast.FuncDefNode;
import jcc.ast.IntLiteralNode;
import jcc.ast.ReturnNode;
import jcc.ast.VarDefNode;

public interface NodeVisitor<E, S> {
    // expr
    E visit(IntLiteralNode n);
    E visit(BinOpNode n);
    // stmt
    S visit(BlockNode n);
    S visit(FuncDefNode n);
    S visit(VarDefNode n);
    S visit(ReturnNode n);
}
