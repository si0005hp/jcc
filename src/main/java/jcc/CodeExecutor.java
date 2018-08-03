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
        
        int pc = mainAddr;
        stack.push(-1L); // Return addres of main func
        
        long x, y = 0;
        while (true) {
            Code c = codes.get(pc);
            
            switch (c.getInst()) {
            case PUSH:
                stack.push(c.getOperand());
                break;
            case ADD:
                y = stack.pop(); x = stack.pop();
                stack.push(x + y);
                break;
            case SUB:
                y = stack.pop(); x = stack.pop();
                stack.push(x - y);
                break;
            case MUL:
                y = stack.pop(); x = stack.pop();
                stack.push(x * y);
                break;
            case DIV:
                y = stack.pop(); x = stack.pop();
                stack.push(x / y);
                break;
            case RET:
                if (c.getOperand() == 0) {
                    y = stack.pop();
                }
                pc = stack.pop().intValue();
                break;
            case ENTRY:
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
