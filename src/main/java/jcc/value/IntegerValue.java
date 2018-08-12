package jcc.value;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = false)
@AllArgsConstructor(staticName = "of")
public class IntegerValue implements JccValue {
    private long val;
    
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
