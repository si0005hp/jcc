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

public class Main {
    private static final String MODE = "view";
    
    public static void main(String[] args) throws IOException {
        try (InputStream is = args.length < 1 ?
                        System.in : new FileInputStream(args[0]);) {
            
            if (MODE.equals("view")) {
                treeView(CharStreams.fromStream(is));
            } else {
                run(CharStreams.fromStream(is));
            }
        }
    }
    
    static int run(CharStream input) {
        JccParser parser = getParser(input);
        parser.setErrorHandler(new BailErrorStrategy());
        ProgramContext p = parser.program();
        return 0;
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
