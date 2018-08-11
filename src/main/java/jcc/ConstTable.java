package jcc;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.stream.Collectors;

import lombok.Value;

public class ConstTable {
    @Value
    public static class ConstEntry {
        private String val;
        private int symbol;
    }

    private final Map<String, ConstEntry> table = new LinkedHashMap<>();

    public int add(String s) {
        ConstEntry e = table.get(s);
        if (e == null) {
            e = new ConstEntry(s, table.size());
            table.put(s, e);
            
        }
        return e.getSymbol();
    }

    public ConstEntry get(int symbol) {
        return table.values().stream().collect(Collectors.toList()).get(symbol);
    }

}
