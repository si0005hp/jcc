package jcc;

import jcc.ast.VarDefNode;
import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class LvarDefinition {
    private CType type;
    private String vname;
    private int fIdx;
    
    public LvarDefinition(VarDefNode n, int fIdx) {
        this.type = n.getType();
        this.vname = n.getVname();
        this.fIdx = fIdx;
    }
}
