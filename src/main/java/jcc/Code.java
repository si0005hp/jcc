package jcc;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.RequiredArgsConstructor;

@Data
@RequiredArgsConstructor
@AllArgsConstructor
public class Code {
    public enum Instruction {
        PUSH,
        PUSHS,
        RET,
        ENTRY,
        ADD,
        SUB,
        MUL,
        DIV,
        MOD,
        BSHIFT,
        FRAME,
        STOREL,
        LOADL,
        CALL,
        POPR,
        STOREA,
        LOADA,
        JZ,
        JMP,
        LABEL,
        CMP,
        PRINTF, // Temporal
    }
    
    private final Instruction inst;
    private MutableLong operand;
}
