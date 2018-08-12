package jcc.value;

import java.util.Arrays;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

@Data
@EqualsAndHashCode(callSuper = false)
@NoArgsConstructor
@AllArgsConstructor
public class PointerValue<T> implements JccValue {
    private int idx = 0;
    protected T[] p;
    
    @Override
    public IntegerValue integer() {
        throw new RuntimeException();
    }
    
    @Override
    public PointerValue<T> pointer() {
        return this;
    }
    
    @Override
    public String toString() {
        return String.format("%s: %s", idx, Arrays.toString(p));
    }
}


