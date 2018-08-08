package jcc;

import java.util.LinkedList;
import java.util.List;
import java.util.Optional;

import org.stringtemplate.v4.compiler.CodeGenerator.conditional_return;

public class CodeExecutor {
    
    private final LinkedList<Long> stack = new LinkedList<>();
    
    private final List<Code> codes;
    private final int mainAddr;
    
    public CodeExecutor(CodeGenerator gen) {
        this.codes = gen.getCodes();
        this.mainAddr = gen.getFuncDefs().get("main").getFuncAddr().asInt();
    }
    
    public int execute() {
        int fp = 0;
        int pc = mainAddr;
        stack.add(-1L); // Return addres of main func
        
        long x, y = 0;
        
        while (pc > -1 && pc <= codes.size() - 1) {
            Code c = codes.get(pc);
            
            switch (c.getInst()) {
            case ENTRY:
                break;
            case PUSH:
                stack.add(c.getOperand().getVal());
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
                if (c.getOperand().getVal() == 0) {
                    y = stack.removeLast();
                }
                int sp = stack.size() - 1;
                for (int i = 0; i < sp - fp; i++) {
                    stack.removeLast(); // Revert expanded stack
                }
                fp = stack.removeLast().intValue();
                pc = stack.removeLast().intValue();
                continue;
            case FRAME:
                stack.add(Long.valueOf(fp));
                fp = stack.size() - 1;
                for (int i = 0; i < c.getOperand().getVal(); i++) {
                    stack.add(null); // Expand stack by the number of lvars
                }
                break;
            case STOREL:
                y = stack.removeLast();
                stack.set(fp + c.getOperand().asInt(), y);
                break;
            case LOADL:
                y = Optional.ofNullable(stack.get(fp + c.getOperand().asInt()))
                    .orElse(0L); // The case that the variable is not initialized
                stack.add(y);
                break;
            case CALL:
                stack.add(Long.valueOf(pc + 1));
                pc = c.getOperand().asInt();
                continue;
            case POPR:
                for (int i = 0; i < c.getOperand().asInt(); i++) {
                    stack.removeLast(); // Revert expanded stack
                }
                stack.add(y);
                break;
            case STOREA:
                y = stack.removeLast();
                stack.set(fp - (c.getOperand().asInt() + 1), y);
                break;
            case LOADA:
                y = stack.get(fp - (c.getOperand().asInt() + 1));
                stack.add(y);
                break;
            case JZ:
                x = stack.removeLast().intValue();
                if (x == 0) {
                    pc = c.getOperand().asInt();
                    continue;
                }
                break;
            case JMP:
                pc = c.getOperand().asInt();
                continue;
            case LABEL:
                break;
            default:
                throw new IllegalArgumentException(c.getInst().name());
            }
            pc++;
        }
        
        return Long.valueOf(y).intValue();
    }

}
