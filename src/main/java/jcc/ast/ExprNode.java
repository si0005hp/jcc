package jcc.ast;

import jcc.NodeVisitor;

public abstract class ExprNode extends Node {
    public abstract <E, S> E accept(NodeVisitor<E, S> v);
}
