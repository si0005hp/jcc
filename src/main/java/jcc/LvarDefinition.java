package jcc;

import jcc.type.Type;
import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class LvarDefinition {
    private Type type;
    private String vname;
    private boolean isArg; // True if it's function arg
    private int idx; // Idx of bp
}
