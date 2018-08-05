package jcc;

import java.util.LinkedList;
import java.util.List;

import jcc.code.Code;

public class CodeExecutor {
    
    private final LinkedList<Long> stack = new LinkedList<>();
    
    private final List<Code> codes;
    private final int mainAddr;
    
    public CodeExecutor(CodeGenerator gen) {
        this.codes = gen.getCodes();
        this.mainAddr = gen.getFuncDefs().get("main").getFuncAddr();
    }
    
    public int execute() {
        int fp = 0;
        int pc = mainAddr;
        stack.add(-1L); // Return addres of main func
        
        long x, y = 0;
        while (true) {
            Code c = codes.get(pc);
            
            switch (c.getInst()) {
            case ENTRY:
                break;
            case PUSH:
                stack.add(c.getOperand());
                break;
            case ADD:
                y = stack.removeLast(); x = stack.removeLast();
                stack.add(x + y);
                break;
            case SUB:
                y = stack.removeLast(); x = stack.removeLast();
                stack.add(x - y);
                break;
            case MUL:
                y = stack.removeLast(); x = stack.removeLast();
                stack.add(x * y);
                break;
            case DIV:
                y = stack.removeLast(); x = stack.removeLast();
                stack.add(x / y);
                break;
            case RET:
                if (c.getOperand() == 0) {
                    y = stack.removeLast();
                }
                int sp = stack.size() - 1;
                for (int i = 0; i < sp - fp; i++) {
                    stack.removeLast(); // Revert expanded stack
                }
                fp = stack.removeLast().intValue();
                pc = stack.removeLast().intValue();
                break;
            case FRAME:
                stack.add(Long.valueOf(fp));
                fp = stack.size() - 1;
                for (int i = 0; i < c.getOperand(); i++) {
                    stack.add(null); // Expand stack by the number of lvars
                }
                break;
            case STOREL:
                y = stack.removeLast();
                stack.set(fp + (int)c.getOperand(), y);
                break;
            case LOADL:
                y = stack.get(fp + (int)c.getOperand());
                stack.add(y);
                break;
            default:
                throw new IllegalArgumentException(c.getInst().name());
            }
            if (pc == -1) {
                break;
            }
            pc++;
        }
        
        return Long.valueOf(y).intValue();
    }

}
