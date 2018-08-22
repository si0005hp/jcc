package jcc;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.ListIterator;
import java.util.Map;
import java.util.Optional;

import jcc.ast.FuncDefNode;
import jcc.ast.VarDefNode;
import lombok.Getter;

@Getter
public class FunctionScope {
    private final LinkedList<Map<String, LvarDefinition>> scopeStack = new LinkedList<>();
    private final LinkedList<MutableNum> breakStack = new LinkedList<>();
    private final LinkedList<MutableNum> continueStack = new LinkedList<>();

    private int argIdx = 0;
    private int lvarIdx = 0;

    public FunctionScope(FuncDefNode n) {
        pushScope(); // This initial scope is the layer of func args
        addFuncArgs(n); // Register params to scope
    }
    
    /**
     * Supposed to be called by each block starts
     */
    public void pushScope() {
        scopeStack.add(new HashMap<>());
    }

    /**
     * Supposed to be called by each block ends
     */
    public void popScope() {
        scopeStack.removeLast();
    }

    private void addFuncArgs(FuncDefNode n) {
        n.getParams().stream()
            .map(p -> new LvarDefinition(p.getType(), p.getPname(), true, ++argIdx))
            .forEach(this::addVarDef);
    }
    
    public LvarDefinition addVar(VarDefNode v) {
        return addVarDef(new LvarDefinition(v.getType(), v.getVname(), false, v.getIdx() * -1));
    }

    private LvarDefinition addVarDef(LvarDefinition v) {
        if (findVar(v.getVname()).isPresent()) {
            throw new RuntimeException("Duplicated variable declaration: " + v.getVname());
        }
        scopeStack.getLast().put(v.getVname(), v);
        return v;
    }

    public LvarDefinition getVar(String vname) {
        return findVar(vname).orElseThrow(() -> new RuntimeException("Unresolved variable: " + vname));
    }

    private Optional<LvarDefinition> findVar(String vname) {
        for (ListIterator<Map<String, LvarDefinition>> it = scopeStack.listIterator(scopeStack.size()); it
                .hasPrevious();) {
            Map<String, LvarDefinition> scope = it.previous();
            if (scope.containsKey(vname)) {
                return Optional.of(scope.get(vname));
            }
        }
        return Optional.empty();
    }

    public void pushBreak(MutableNum exitAddr) {
        breakStack.add(exitAddr);
    }

    public void popBreak() {
        breakStack.removeLast();
    }

    public MutableNum getBreakPoint() {
        return breakStack.getLast();
    }

    public void pushContinue(MutableNum entAddr) {
        continueStack.add(entAddr);
    }

    public void popContinue() {
        continueStack.removeLast();
    }

    public MutableNum getContinuePoint() {
        return continueStack.getLast();
    }
}
