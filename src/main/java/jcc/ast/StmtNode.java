package jcc.ast;

import jcc.NodeVisitor;

public abstract class StmtNode extends Node {
    public abstract <E, S> S accept(NodeVisitor<E, S> v);
}
