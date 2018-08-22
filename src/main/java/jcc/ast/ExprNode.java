package jcc.ast;

import jcc.NodeVisitor;
import jcc.type.Type;

public abstract class ExprNode extends Node {
    public abstract Type type();
    public abstract <E, S> E accept(NodeVisitor<E, S> v);
}
