package jcc.ast;

import jcc.CType;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = false)
@AllArgsConstructor
public class VarDefNode extends StmtNode {
    private CType type;
    private String vname;
}
