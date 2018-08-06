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
    
    @Test
    public void var() {
        assertThat(runF("var/var1.c"), is(9));
        assertThat(runF("var/var2.c"), is(21));
        assertThat(runF("var/var3.c"), is(30));
        assertThat(runF("var/var4.c"), is(80));
    }
    
    @Test
    public void func() {
        assertThat(runF("func/func1.c"), is(9));
        assertThat(runF("func/func2.c"), is(11));
        assertThat(runF("func/func3.c"), is(9));
        assertThat(runF("func/func4.c"), is(8));
        assertThat(runF("func/func5.c"), is(20));
    }
    
    private int runF(String s) {
        try (InputStream is = getClass().getResourceAsStream(s)) {
            return Jcc.run(CharStreams.fromStream(is));
        } catch (IOException e) {
            throw new RuntimeException("Failed to load file: " + s);
        }
    }
}
