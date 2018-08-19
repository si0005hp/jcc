package jcc.type;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = false)
@AllArgsConstructor(staticName = "of")
public class VoidType extends Type {
    @Override
    public int getSize() {
        throw new UnsupportedOperationException();
    }
}
