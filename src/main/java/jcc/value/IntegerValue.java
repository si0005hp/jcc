package jcc.value;

import jcc.CType;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = false)
@AllArgsConstructor(staticName = "of")
public class IntegerValue implements JccValue {
    private CType cType;
    private long val;
    
    public static IntegerValue of(long val) {
        return new IntegerValue(CType.INT, val);
    }
    
    public int asInt() {
        return (int)val;
    }
    
    @Override
    public String toString() {
        return String.valueOf(val);
    }

    @Override
    public IntegerValue integer() {
        return this;
    }

    @Override
    public PointerValue<?> pointer() {
        throw new RuntimeException();
    }
}
