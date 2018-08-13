package jcc.ast;

import java.util.List;

import jcc.NodeVisitor;
import jcc.type.Type;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.Value;

@Data
@EqualsAndHashCode(callSuper = false)
@AllArgsConstructor
public class FuncDefNode extends StmtNode {
    @Value
    public static class ParamDef {
        private Type type;
        private String pname;
    }

    private Type retvalType;
    private String fname;
    private List<ParamDef> params;
    private BlockNode block;

    @Override
    public <E, S> S accept(NodeVisitor<E, S> v) {
        return v.visit(this);
    }
}
