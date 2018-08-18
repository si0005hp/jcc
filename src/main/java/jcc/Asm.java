package jcc;

import java.io.BufferedWriter;
import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import lombok.Getter;
import lombok.Value;

@Getter
public class Asm {
    @Value(staticConstructor = "of")
    static class Code {
        String format;
        Object[] args;
        
        @Override
        public String toString() {
            return String.format(format, args);
        }
    }
    
    private final List<Code> codes = new ArrayList<>();
    
    public void gen(String format, Object ... args) {
        codes.add(Code.of(format, args));
    }
    
    public void gent(String format, Object ... args) {
        gen("\t" + format, args);
    }
    
    @Override
    public String toString() {
        return codes.stream().map(Code::toString).collect(Collectors.joining(System.lineSeparator()))
                + System.lineSeparator();
    }
    
    public void write(OutputStream out) {
        try (OutputStreamWriter osw = new OutputStreamWriter(out);
                BufferedWriter bw = new BufferedWriter(osw)) {
            bw.write(this.toString());
        } catch (IOException e) {
            throw new RuntimeException("Failed to write.", e);
        }
    }
    
}
