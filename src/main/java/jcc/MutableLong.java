package jcc;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor(staticName = "of")
public class MutableLong {
    private long val;
    
    public int asInt() {
        return (int)val;
    }
    
    public void inc() {
        this.val++;
    }
    
    public void dec() {
        this.val--;
    }
    
    @Override
    public String toString() {
        return String.valueOf(val);
    }
}
