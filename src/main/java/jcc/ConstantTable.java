package jcc;

import java.util.LinkedHashMap;
import java.util.Map;

import lombok.Getter;

@Getter
public class ConstantTable {
    private int strLblIdx = 0;
    private final Map<String, String> strLiterals = new LinkedHashMap<>();
    
    public String registerStrLiteral(String s) {
        if (strLiterals.containsKey(s)) {
            return strLiterals.get(s);
        }
        String lbl = makeStrLbl();
        strLiterals.put(s, lbl);
        return lbl;
    }
    
    private String makeStrLbl() {
        return String.format(".LC%d", strLblIdx++);
    }
}
