package jcc.type;

import jcc.CType;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = false)
@AllArgsConstructor(staticName = "of")
public class PointerType extends Type {
    private CType baseType;

    @Override
    public int getSize() {
        return 8;
    }
}
