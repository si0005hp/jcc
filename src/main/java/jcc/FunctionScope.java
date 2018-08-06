package jcc;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.ListIterator;
import java.util.Map;
import java.util.Optional;

public class FunctionScope {
    private final LinkedList<Map<String, LvarDefinition>> scopeStack = new LinkedList<>();
    
    public FunctionScope() {
        scopeStack.add(new HashMap<>()); // This initial map is the layer of func args
    }
    
    /**
     * Supposed to be called by each block starts
     */
    public void addScope() {
        scopeStack.add(new HashMap<>());
    }
    
    /**
     * Supposed to be called by each block ends
     */
    public void removeScope() {
        scopeStack.removeLast();
    }
    
    public LvarDefinition addVar(CType type, String vname, boolean isArg) {
        if (findVar(vname).isPresent()) {
            throw new RuntimeException("Duplicated variable declaration: " + vname);
        }
        Map<String, LvarDefinition> scope = scopeStack.getLast();
        LvarDefinition ld = new LvarDefinition(type, vname, isArg, scope.size() + 1);
        scope.put(vname, ld);
        return ld;
    }
    
    public LvarDefinition getVar(String vname) {
        return findVar(vname).orElseThrow(() -> new RuntimeException("Unresolved variable: " + vname));
    }
    
    private Optional<LvarDefinition> findVar(String vname) {
        for (ListIterator<Map<String, LvarDefinition>> it = scopeStack.listIterator(scopeStack.size());
                it.hasPrevious();) {
            Map<String, LvarDefinition> scope = it.previous();
            if (scope.containsKey(vname)) {
                return Optional.of(scope.get(vname));
            }
        }
        return Optional.empty();
    }
}
