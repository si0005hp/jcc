package jcc.type;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = false)
@AllArgsConstructor(staticName = "of")
public class PointerType extends Type {
    private Type baseType;

    @Override
    public int getSize() {
        return 8;
    }
    
    @Override
    public Type baseType() {
        return baseType;
    }
}
