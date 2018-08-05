package jcc;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.ListIterator;
import java.util.Map;

public class FunctionScope {
    private final LinkedList<Map<String, LvarDefinition>> scopeStack = new LinkedList<>();
    
    public void addScope() {
        scopeStack.add(new HashMap<>());
    }
    
    public void removeScope() {
        scopeStack.removeLast();
    }
    
    public LvarDefinition addVar(CType type, String vname) {
        Map<String, LvarDefinition> scope = scopeStack.getLast();
        LvarDefinition ld = new LvarDefinition(type, vname, scope.size() + 1);
        scope.put(vname, ld);
        return ld;
    }
    
    public LvarDefinition getVar(String vname) {
        for (ListIterator<Map<String, LvarDefinition>> it = scopeStack.listIterator(scopeStack.size());
                it.hasPrevious();) {
            Map<String, LvarDefinition> scope = it.previous();
            if (scope.containsKey(vname)) {
                return scope.get(vname);
            }
        }
        throw new RuntimeException("Unresolved variable: " + vname);
    }
}
