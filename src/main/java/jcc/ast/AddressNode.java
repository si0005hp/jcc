package jcc.ast;

import jcc.NodeVisitor;
import jcc.type.Type;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.RequiredArgsConstructor;

@Data
@EqualsAndHashCode(callSuper = false)
@AllArgsConstructor
@RequiredArgsConstructor
public class AddressNode extends ExprNode {
    private final VarRefNode var;
    private Type type;

    @Override
    public <E, S> E accept(NodeVisitor<E, S> v) {
        return v.visit(this);
    }

    @Override
    public Type type() {
        return type;
    }
}
