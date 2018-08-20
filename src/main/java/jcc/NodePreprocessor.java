package jcc;

import jcc.ast.ProgramNode;
import jcc.ast.StrLiteralNode;
import lombok.Getter;

@Getter
public class NodePreprocessor {
    private final ConstantTable constTbl = new ConstantTable();

    public void preProcess(ProgramNode n) {
        processConstants(n);
        processFVarAnalyze(n);
    }
    
    private void processConstants(ProgramNode n) {
        for (StrLiteralNode sn : n.getStrs()) {
            String lbl = constTbl.registerStrLiteral(sn.getVal());
            sn.setLbl(lbl);
        }
    }
    
    private void processFVarAnalyze(ProgramNode n) {
        new FuncVarAnalyzer().visit(n);
    }

}
