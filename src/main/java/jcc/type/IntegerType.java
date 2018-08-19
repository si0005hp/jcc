package jcc.type;

import jcc.CType;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = false)
@AllArgsConstructor(staticName = "of")
public class IntegerType extends Type {
    private CType baseType;
    
    @Override
    public int getSize() {
        switch (baseType) {
        case INT: return 4;
        case CHAR: return 1;
        default: 
            throw new IllegalArgumentException(baseType.name());
        }
    }
}
