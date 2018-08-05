package jcc;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;

import org.antlr.v4.gui.TreeViewer;
import org.antlr.v4.runtime.BailErrorStrategy;
import org.antlr.v4.runtime.CharStream;
import org.antlr.v4.runtime.CharStreams;
import org.antlr.v4.runtime.CommonTokenStream;

import jcc.JccParser.ProgramContext;

public class Jcc {
    private static final String MODE = "v";
    
    public static void main(String[] args) throws IOException {
        try (InputStream is = args.length < 1 ?
                        System.in : new FileInputStream(args[0]);) {

            if (MODE.equals("v")) {
                treeView(CharStreams.fromStream(is));
            } else if (MODE.equals("d")) {
                debugCode(CharStreams.fromStream(is));
            } else {
                int result = run(CharStreams.fromStream(is));
                System.out.println(result);
            }
        }
    }
    
    static int run(CharStream input) {
        JccParser parser = getParser(input);
        parser.setErrorHandler(new BailErrorStrategy());
        ProgramContext p = parser.program();
        
        CodeGenerator gen = new CodeGenerator();
        gen.generate(p.n);
        CodeExecutor exec = new CodeExecutor(gen);
        return exec.execute();
    }
    
    static void debugCode(CharStream input) {
        JccParser parser = getParser(input);
        parser.setErrorHandler(new BailErrorStrategy());
        ProgramContext p = parser.program();
        
        CodeGenerator gen = new CodeGenerator();
        gen.generate(p.n);
        gen.debugCode();
    }
    
    private static JccParser getParser(CharStream input) {
        JccLexer lexer = new JccLexer(input);
        JccParser parser = new JccParser(new CommonTokenStream(lexer));
        return parser;
    }
    
    private static void treeView(CharStream input) {
        JccParser parser = getParser(input);
        TreeViewer v = new TreeViewer(Arrays.asList(parser.getRuleNames()), parser.program());
        v.open();
    }

}
