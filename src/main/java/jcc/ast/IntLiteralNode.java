package jcc.ast;

import jcc.NodeVisitor;
import jcc.type.Type;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = false)
@AllArgsConstructor
public class IntLiteralNode extends ExprNode {
    private Type type;
    private long val;

    @Override
    public <E, S> E accept(NodeVisitor<E, S> v) {
        return v.visit(this);
    }

    @Override
    public Type type() {
        return type;
    }
}
