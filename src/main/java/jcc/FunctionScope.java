package jcc;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.ListIterator;
import java.util.Map;
import java.util.Optional;

import lombok.Getter;

@Getter
public class FunctionScope {
    private final LinkedList<Map<String, LvarDefinition>> scopeStack = new LinkedList<>();
    
    private int argIdx = 0;
    private int lvarIdx = 0;
    
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
    
    public LvarDefinition addArg(CType type, String vname) {
        return addVar(new LvarDefinition(type, vname, true, ++argIdx));
    }
    
    public LvarDefinition addLvar(CType type, String vname) {
        return addVar(new LvarDefinition(type, vname, false, ++lvarIdx));
    }
    
    private LvarDefinition addVar(LvarDefinition ld) {
        if (findVar(ld.getVname()).isPresent()) {
            throw new RuntimeException("Duplicated variable declaration: " + ld.getVname());
        }
        scopeStack.getLast().put(ld.getVname(), ld);
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
