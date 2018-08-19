package jcc;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.ListIterator;
import java.util.Map;
import java.util.Optional;

import jcc.ast.FuncDefNode;
import jcc.type.Type;
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
        n.getParams().forEach(p -> addArg(p.getType(), p.getPname())); // Register params to scope
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

    public LvarDefinition addArg(Type type, String vname) {
        return addVar(new LvarDefinition(type, vname, true, ++argIdx));
    }

    public LvarDefinition addLvar(Type type, String vname) {
        return addVar(new LvarDefinition(type, vname, false, ++lvarIdx * -8));
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
