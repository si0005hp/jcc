package jcc;

import jcc.ast.VarDefNode;
import jcc.type.Type;
import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class LvarDefinition {
    private Type type;
    private String vname;
    private boolean isArg; // True if it's function arg
    private int idx; // Idx of fp or sp

    public LvarDefinition(VarDefNode n, int idx) {
        this.type = n.getType();
        this.vname = n.getVname();
        this.idx = idx;
    }
}
