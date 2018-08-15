package jcc;

import jcc.value.JccValue;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.RequiredArgsConstructor;

@Data
@RequiredArgsConstructor
@AllArgsConstructor
public class Code {
    public enum Instruction {
        PUSH,
        PUSHP,
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
        LOADLA,
        LOADLP,
        CALL,
        POPR,
        STOREA,
        LOADA,
        LOADAA,
        LOADAP,
        JZ,
        JMP,
        LABEL,
        CMP,
        PRINTF, // Temporal
    }
    
    private final Instruction inst;
    private JccValue operand;
}
