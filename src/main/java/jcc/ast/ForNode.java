package jcc.ast;

import jcc.NodeVisitor;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = false)
@AllArgsConstructor
public class ForNode extends StmtNode {
    private ExprNode init;
    private ExprNode cond;
    private ExprNode step;
    private StmtNode body;

    @Override
    public <E, S> S accept(NodeVisitor<E, S> v) {
        return v.visit(this);
    }
}
