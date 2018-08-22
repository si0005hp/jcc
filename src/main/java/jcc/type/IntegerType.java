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
        case CHAR: return 1;
        case SHORT: return 2;
        case INT: return 4;
        case LONG: return 8;
        default: 
            throw new IllegalArgumentException(baseType.name());
        }
    }
    
    @Override
    public Type baseType() {
        throw new UnsupportedOperationException();
    }
}
