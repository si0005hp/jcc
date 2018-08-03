package jcc;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

import java.io.IOException;
import java.io.InputStream;

import org.antlr.v4.runtime.CharStreams;
import org.junit.Test;


public class JccTest {

    @Test
    public void arithmetic() {
        assertThat(runF("arithmetic/arithmetic1.c"), is(0));
        assertThat(runF("arithmetic/arithmetic2.c"), is(18));
        assertThat(runF("arithmetic/arithmetic3.c"), is(16));
    }
    
    private int runF(String s) {
        try (InputStream is = getClass().getResourceAsStream(s)) {
            return Jcc.run(CharStreams.fromStream(is));
        } catch (IOException e) {
            throw new RuntimeException("Failed to load file: " + s);
        }
    }
}
