package jcc;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class StrUtils {
    
    public static Character[] strToCharacterArray(String s) {
        List<Character> l = new ArrayList<>();
        for (char c : s.toCharArray()) {
            l.add(new Character(c));
        }
        return l.toArray(new Character[0]);
    }
    
    public static String characterArrayToStr(Character[] c) {
        StringBuilder sb = new StringBuilder();
        Arrays.asList(c).stream().forEach(sb::append);
        return sb.toString();
    }
    
    public static String stringValue(String _text) {
        int pos = 0;
        int idx;
        StringBuffer buf = new StringBuffer();
        String text = _text.substring(1, _text.length() - 1);

        while ((idx = text.indexOf("\u005c\u005c", pos)) >= 0) {
            buf.append(text.substring(pos, idx));
            if (text.length() >= idx + 4 
                    && Character.isDigit(text.charAt(idx + 1))
                    && Character.isDigit(text.charAt(idx + 2)) 
                    && Character.isDigit(text.charAt(idx + 3))) {
                buf.append(unescapeOctal(text.substring(idx + 1, idx + 4)));
                pos = idx + 4;
            } else {
                buf.append(unescapeSeq(text.charAt(idx + 1)));
                pos = idx + 2;
            }
        }
        if (pos < text.length()) {
            buf.append(text.substring(pos, text.length()));
        }
        return buf.toString();
    }
    
    private static final int charMax = 255;
    
    private static char unescapeOctal(String digits) {
        int i = Integer.parseInt(digits, 8);
        if (i > charMax) {
            throw new RuntimeException(
                "octal character sequence too big: \u005c\u005c" + digits);
        }
        return (char)i;
    }
    
    private static final char bell = 7;
    private static final char backspace = 8;
    private static final char escape = 27;
    private static final char vt = 11;
    
    private static char unescapeSeq(char c) {
        switch (c) {
        case '0': return '\u005c0';
        case '"': return '"';
        case '\u005c'': return '\u005c'';
        case 'a': return bell;
        case 'b': return backspace;
        case 'e': return escape;
        case 'f': return '\u005cf';
        case 'n': return '\u005cn';
        case 'r': return '\u005cr';
        case 't': return '\u005ct';
        case 'v': return vt;
        default:
            throw new RuntimeException("unknown escape sequence: \u005c"\u005c\u005c" + c);
        }
    }
    
    public static long characterCode(String text) {
        String s = stringValue(text);
        return (long)s.charAt(s.length() - 1);
    }
}
