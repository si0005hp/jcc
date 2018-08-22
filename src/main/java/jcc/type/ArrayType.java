package jcc.type;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = false)
@AllArgsConstructor(staticName = "of")
public class ArrayType extends Type {
    private Type baseType;
    private int size;

    @Override
    public int getSize() {
        return baseType.getSize() * size;
    }

    @Override
    public Type baseType() {
        return baseType;
    }
}
