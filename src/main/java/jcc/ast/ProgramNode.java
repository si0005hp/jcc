package jcc.ast;

import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = false)
@AllArgsConstructor
public class ProgramNode extends Node {
    private List<StrLiteralNode> strs;
    private List<FuncDefNode> funcDefs;
}
