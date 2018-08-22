package jcc.ast;

import java.util.List;

import jcc.NodeVisitor;
import jcc.type.Type;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = false)
@AllArgsConstructor
public class ArrLiteralNode extends ExprNode {
    private Type type;
    private List<ExprNode> elems;

    @Override
    public <E, S> E accept(NodeVisitor<E, S> v) {
        return v.visit(this);
    }

    @Override
    public Type type() {
        return type;
    }
}
