package jcc;

import java.util.List;

import jcc.ast.FuncDefNode;
import jcc.ast.FuncDefNode.ParamDef;
import jcc.type.Type;
import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class FuncDefinition {
    private Type retvalType;
    private String fname;
    private List<ParamDef> params;
    private MutableNum funcAddr;

    public FuncDefinition(FuncDefNode n, MutableNum addr) {
        this.retvalType = n.getRetvalType();
        this.fname = n.getFname();
        this.params = n.getParams();
        this.funcAddr = addr;
    }
}
