package jcc;

import java.util.List;

import jcc.ast.FuncDefNode;
import jcc.ast.FuncDefNode.ParamDef;
import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class FuncDefinition {
    private CType retvalType;
    private String fname;
    private List<ParamDef> params;
    private MutableLong funcAddr;
    
    public FuncDefinition(FuncDefNode n, MutableLong addr) {
        this.retvalType = n.getRetvalType();
        this.fname = n.getFname();
        this.params = n.getParams();
        this.funcAddr = addr;
    }
}
