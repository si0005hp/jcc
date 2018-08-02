package jcc.ast;

import java.util.List;

import jcc.CType;
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
        private CType ptype;
        private String pname;
    }
    
    private CType retvalType;
    private String fname;
    private List<ParamDef> params;
    private BlockNode block;
}
