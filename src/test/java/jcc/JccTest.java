package jcc;

import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PrintStream;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Predicate;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.antlr.v4.runtime.CharStreams;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.StringUtils;
import org.junit.BeforeClass;
import org.junit.Test;

import lombok.Value;

public class JccTest {

    private static final List<TestF> tests = new ArrayList<>();
    private static final String PTN = ".*";
    
    private static final String TEST_LIST = "test.list";
    private static final String TEST_OUT_DIR = ".test";
    
    @Value
    static class TestF {
        String name;
        String answer;
    }
    
    @BeforeClass
    public static void setup() throws IOException, URISyntaxException {
        File outputDir = new File(getOutputDirPath());
        FileUtils.deleteQuietly(outputDir);
        FileUtils.forceMkdir(outputDir);
        
        File testListFile = new File(JccTest.class.getResource(TEST_LIST).toURI());
        readTestList(testListFile, tests);
        FileUtils.copyFileToDirectory(testListFile, outputDir);
    }
    
    @Test
    public void run() {
        List<TestF> targetTests = tests.stream().filter(testFilter(PTN)).collect(Collectors.toList());
        for (TestF f : targetTests) {
            if ("@Fail".equals(f.getAnswer())) {
                expectedToFail(() -> runF(f.getName())); 
            } else {
                runF(f.getName());
            }
        }
        System.out.println(String.format("Processed %s files with pattern '%s'.", targetTests.size(), PTN));
    }
    
    private Predicate<TestF> testFilter(String pattern) {
        return t -> Pattern.matches(pattern, t.getName());
    }
    
    private static void readTestList(File f, List<TestF> testList) {
        try (InputStream is = new FileInputStream(f);
                InputStreamReader isr = new InputStreamReader(is);
                BufferedReader br = new BufferedReader(isr);) {
            String line = null;
            while (StringUtils.isNotEmpty((line = br.readLine()))) {
                testList.add(new TestF(line.split("\t")[0], line.split("\t")[1]));
            }
        } catch (IOException e) {
            throw new RuntimeException("Failed to load file: " + f.getName());
        }
    }

    private static String getOutputDirPath() {
        return System.getProperty("user.dir") + File.separator + TEST_OUT_DIR; 
    }
    
    private static String getOutputFilePath(String fname) {
        return getOutputDirPath() + File.separator + getSFileName(fname); 
    }
    
    private void runF(String cFileName) {
        try (InputStream is = getClass().getResourceAsStream(cFileName);
                FileOutputStream fos = new FileOutputStream(getOutputFilePath(cFileName));) {
            Jcc.run(CharStreams.fromStream(is), fos);
        } catch (IOException e) {
            throw new RuntimeException("Failed to load file: " + cFileName);
        }
    }
    
    private static String getSFileName(String cFileName) {
        if (!cFileName.endsWith(".c")) {
            throw new RuntimeException("Illegal cFileName: " + cFileName);
        }
        return cFileName.replaceFirst("\\.c", ".s");
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
    
    private String runAndGetSysout(Runnable r) {
        PrintStream orgSysout = System.out;

        String result = null;
        try (ByteArrayOutputStream bos = new ByteArrayOutputStream();
                PrintStream ps = new PrintStream(new BufferedOutputStream(bos));) {
            System.setOut(ps);
            r.run();
            System.out.flush();
            result = bos.toString();
        } catch (IOException e) {
            throw new RuntimeException(e);
        } finally {
            System.setOut(orgSysout);
        }
        return result;
    }
    
    private String perNewLine(Object... data) {
        if (data.length == 0) {
            return "";
        }
        return Stream.of(data).map(String::valueOf).collect(Collectors.joining("\n")) + "\n";
    }
}
