package jcc;

import java.util.List;

import jcc.ast.FuncDefNode;
import jcc.ast.FuncDefNode.ParamDef;
import jcc.type.Type;
import jcc.value.IntegerValue;
import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class FuncDefinition {
    private Type retvalType;
    private String fname;
    private List<ParamDef> params;
    private IntegerValue funcAddr;

    public FuncDefinition(FuncDefNode n, IntegerValue addr) {
        this.retvalType = n.getRetvalType();
        this.fname = n.getFname();
        this.params = n.getParams();
        this.funcAddr = addr;
    }
}
