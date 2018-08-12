package jcc;

import static jcc.JccParser.EQEQ;
import static jcc.JccParser.GT;
import static jcc.JccParser.GTE;
import static jcc.JccParser.LSHIFT;
import static jcc.JccParser.LT;
import static jcc.JccParser.LTE;
import static jcc.JccParser.NOTEQ;
import static jcc.JccParser.RSHIFT;

import java.util.LinkedList;
import java.util.List;
import java.util.Optional;

import jcc.value.IntegerValue;
import jcc.value.JccValue;
import jcc.value.PointerValue;

public class CodeExecutor {
    
    private final LinkedList<JccValue> stack = new LinkedList<>();
    
    private final List<Code> codes;
    private final int mainAddr;
    
    public CodeExecutor(CodeGenerator gen) {
        this.codes = gen.getCodes();
        this.mainAddr = gen.getFuncDefs().get("main").getFuncAddr().asInt();
    }
    
    public int execute() {
        int fp = 0;
        int pc = mainAddr;
        stack.add(IntegerValue.of(-1)); // Return addres of main func
        
        JccValue x, y = null;
        
        while (pc > -1 && pc <= codes.size() - 1) {
            Code c = codes.get(pc);
            
            switch (c.getInst()) {
            case ENTRY:
                break;
            case PUSH:
                stack.add(c.getOperand());
                break;
            case PUSHP:
                stack.add(c.getOperand());
                break;
            case ADD:
                if (c.getOperand().integer().getVal() == 0) {
                    // Integer Arithmetic
                    y = stack.removeLast(); x = stack.removeLast();
                    stack.add(IntegerValue.of(x.integer().getVal() + y.integer().getVal()));
                } else { 
                    // Pointer Arithmetic
                }
                break;
            case SUB:
                if (c.getOperand().integer().getVal() == 0) {
                    // Integer Arithmetic
                    y = stack.removeLast(); x = stack.removeLast();
                    stack.add(IntegerValue.of(x.integer().getVal() - y.integer().getVal()));
                } else { 
                    // Pointer Arithmetic
                }
                break;
            case MUL:
                y = stack.removeLast(); x = stack.removeLast();
                stack.add(IntegerValue.of(x.integer().getVal() * y.integer().getVal()));
                break;
            case DIV:
                y = stack.removeLast(); x = stack.removeLast();
                stack.add(IntegerValue.of(x.integer().getVal() / y.integer().getVal()));
                break;
            case MOD:
                y = stack.removeLast(); x = stack.removeLast();
                stack.add(IntegerValue.of(x.integer().getVal() % y.integer().getVal()));
                break;
            case BSHIFT:
                y = stack.removeLast(); x = stack.removeLast();
                switch (c.getOperand().integer().asInt()) {
                case LSHIFT: stack.add(IntegerValue.of(x.integer().getVal() << y.integer().getVal())); break;
                case RSHIFT: stack.add(IntegerValue.of(x.integer().getVal() >> y.integer().getVal())); break;
                default:
                    throw new IllegalArgumentException(c.getOperand().toString());
                }
                break;
            case CMP:
                // Currently only suppose Integer 
                y = stack.removeLast(); x = stack.removeLast();
                long xi = x.integer().getVal(); long yi = y.integer().getVal(); 
                switch (c.getOperand().integer().asInt()) {
                case EQEQ: stack.add(xi == yi ?
                        IntegerValue.of(1) : IntegerValue.of(0)); break;
                case NOTEQ: stack.add(xi != yi ?
                        IntegerValue.of(1) : IntegerValue.of(0)); break;
                case GT: stack.add(xi > yi ? 
                        IntegerValue.of(1) : IntegerValue.of(0)); break;
                case LT: stack.add(xi < yi ? 
                        IntegerValue.of(1) : IntegerValue.of(0)); break;
                case GTE: stack.add(xi >= yi ? 
                        IntegerValue.of(1) : IntegerValue.of(0)); break;
                case LTE: stack.add(xi <= yi ? 
                        IntegerValue.of(1) : IntegerValue.of(0)); break;
                default:
                    throw new IllegalArgumentException(c.getOperand().toString());
                }
                break;
            case RET:
                if (c.getOperand().integer().asInt() == 0) {
                    y = stack.removeLast();
                }
                int sp = stack.size() - 1;
                for (int i = 0; i < sp - fp; i++) {
                    stack.removeLast(); // Revert expanded stack
                }
                fp = stack.removeLast().integer().asInt();
                pc = stack.removeLast().integer().asInt();
                continue;
            case FRAME:
                stack.add(IntegerValue.of(fp));
                fp = stack.size() - 1;
                for (int i = 0; i < c.getOperand().integer().asInt(); i++) {
                    stack.add(null); // Expand stack by the number of lvars
                }
                break;
            case STOREL:
                y = stack.removeLast();
                stack.set(fp + c.getOperand().integer().asInt(), y);
                break;
            case LOADL:
                y = Optional.ofNullable(stack.get(fp + c.getOperand().integer().asInt()))
                    .orElse(IntegerValue.of(0)); // The case that the variable is not initialized
                stack.add(y);
                break;
            case CALL:
                stack.add(IntegerValue.of(pc + 1));
                pc = c.getOperand().integer().asInt();
                continue;
            case POPR:
                for (int i = 0; i < c.getOperand().integer().asInt(); i++) {
                    stack.removeLast(); // Revert expanded stack
                }
                stack.add(y);
                break;
            case STOREA:
                y = stack.removeLast();
                stack.set(fp - (c.getOperand().integer().asInt() + 1), y);
                break;
            case LOADA:
                y = stack.get(fp - (c.getOperand().integer().asInt() + 1));
                stack.add(y);
                break;
            case JZ:
                x = stack.removeLast();
                if (x.integer().asInt() == 0) {
                    pc = c.getOperand().integer().asInt();
                    continue;
                }
                break;
            case JMP:
                pc = c.getOperand().integer().asInt();
                continue;
            case LABEL:
                break;
            case PRINTF:
                Object[] args = new Object[c.getOperand().integer().asInt()];
                for (int i = 0; i < c.getOperand().integer().asInt(); i++) {
                    args[i] = stack.removeLast().integer().getVal();
                }
                y = stack.removeLast();
                @SuppressWarnings("unchecked")
                String fmtStr = StrUtils.characterArrayToStr(((PointerValue<Character>) y.pointer()).getP());
                System.out.printf(fmtStr, args);
                break;
            default:
                throw new IllegalArgumentException(c.getInst().name());
            }
            pc++;
        }
        
        return y.integer().asInt();
    }

}
