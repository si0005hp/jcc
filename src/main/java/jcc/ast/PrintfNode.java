package jcc.ast;

import java.util.List;

import jcc.NodeVisitor;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * Temporal node to compile 'printf' in Jcc
 */
@Data
@EqualsAndHashCode(callSuper = false)
@AllArgsConstructor
public class PrintfNode extends StmtNode {
    private ExprNode fmtStr;
    private List<ExprNode> args;

    @Override
    public <E, S> S accept(NodeVisitor<E, S> v) {
        return v.visit(this);
    }
}
