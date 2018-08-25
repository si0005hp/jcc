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
public class FuncDefNode extends StmtNode {
    @Data
    @AllArgsConstructor
    @RequiredArgsConstructor
    public static class ParamDef {
        private final Type type;
        private final String pname;
        private int idx;
    }

    private final Type retvalType;
    private final String fname;
    private final List<ParamDef> params;
    private final BlockNode block;
    private List<VarDefNode> vars;

    @Override
    public <E, S> S accept(NodeVisitor<E, S> v) {
        return v.visit(this);
    }
}
