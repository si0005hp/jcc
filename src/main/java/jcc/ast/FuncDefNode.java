package jcc.ast;

import java.util.List;

import jcc.NodeVisitor;
import jcc.type.Type;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.RequiredArgsConstructor;
import lombok.Value;

@Data
@EqualsAndHashCode(callSuper = false)
@AllArgsConstructor
@RequiredArgsConstructor
public class FuncDefNode extends StmtNode {
    @Value
    public static class ParamDef {
        private Type type;
        private String pname;
    }

    private final Type retvalType;
    private final String fname;
    private final List<ParamDef> params;
    private final BlockNode block;
    private int lvarCnt;

    @Override
    public <E, S> S accept(NodeVisitor<E, S> v) {
        return v.visit(this);
    }
}
