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
}
