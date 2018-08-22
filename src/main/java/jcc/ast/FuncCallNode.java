package jcc.ast;

import java.util.List;

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
public class FuncCallNode extends ExprNode {
    private final String fname;
    private final List<ExprNode> args;
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
