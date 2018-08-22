package jcc.ast;

import jcc.CType;
import jcc.NodeVisitor;
import jcc.type.IntegerType;
import jcc.type.PointerType;
import jcc.type.Type;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.RequiredArgsConstructor;

@Data
@EqualsAndHashCode(callSuper = false)
@AllArgsConstructor
@RequiredArgsConstructor
public class StrLiteralNode extends ExprNode {
    private final String val;
    private String lbl;

    @Override
    public <E, S> E accept(NodeVisitor<E, S> v) {
        return v.visit(this);
    }

    @Override
    public Type type() {
        return PointerType.of(IntegerType.of(CType.CHAR));
    }
}
