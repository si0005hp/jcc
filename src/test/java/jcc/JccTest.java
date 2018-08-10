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
        assertThat(runF("arithmetic/arithmetic4.c"), is(1));
    }
    
    @Test
    public void var() {
        assertThat(runF("var/var1.c"), is(9));
        assertThat(runF("var/var2.c"), is(21));
        assertThat(runF("var/var3.c"), is(30));
        assertThat(runF("var/var4.c"), is(80));
        expectedToFail(() -> runF("var/var5.c"));
        assertThat(runF("var/var6.c"), is(0));
    }
    
    @Test
    public void func() {
        assertThat(runF("func/func1.c"), is(9));
        assertThat(runF("func/func2.c"), is(11));
        assertThat(runF("func/func3.c"), is(9));
        assertThat(runF("func/func4.c"), is(8));
        assertThat(runF("func/func5.c"), is(20));
        expectedToFail(() -> runF("func/func6.c"));
    }
    
    @Test
    public void ifstmt() {
        assertThat(runF("ifstmt/if1.c"), is(15));
        assertThat(runF("ifstmt/if2.c"), is(25));
        assertThat(runF("ifstmt/if3.c"), is(34));
        assertThat(runF("ifstmt/if4.c"), is(149));
    }
    
    @Test
    public void cmp() {
        assertThat(runF("cmp/cmp1.c"), is(1));
        assertThat(runF("cmp/cmp2.c"), is(212));
    }
    
    @Test
    public void whilestmt() {
        assertThat(runF("whilestmt/while1.c"), is(32));
        assertThat(runF("whilestmt/while2.c"), is(32));
        assertThat(runF("whilestmt/while3.c"), is(16));
    }
    
    @Test
    public void others() {
        assertThat(runF("others/expr_stmt.c"), is(9));
    }
    
    private int runF(String s) {
        try (InputStream is = getClass().getResourceAsStream(s)) {
            return Jcc.run(CharStreams.fromStream(is));
        } catch (IOException e) {
            throw new RuntimeException("Failed to load file: " + s);
        }
    }
    
    private void expectedToFail(Runnable r) {
        try {
            r.run();
        } catch (Exception e) {
            System.out.println("Expected error: " + e.getMessage());
            return;
        }
        throw new RuntimeException("Expected to be failed but ended normaly.");
    }
}
