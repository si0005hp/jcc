package jcc.ast;

import java.util.List;

import jcc.NodeVisitor;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = false)
@AllArgsConstructor
public class FuncCallNode extends ExprNode {
    private String fname;
    private List<ExprNode> args;

    @Override
    public <E, S> E accept(NodeVisitor<E, S> v) {
        return v.visit(this);
    }
}
