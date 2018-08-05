package jcc.code;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.RequiredArgsConstructor;

@Data
@RequiredArgsConstructor
@AllArgsConstructor
public class Code {
    public enum Instruction {
        PUSH,
        RET,
        ENTRY,
        ADD,
        SUB,
        MUL,
        DIV,
        FRAME,
        STOREL,
        LOADL,
    }
    
    private final Instruction inst;
    private long operand;
}
