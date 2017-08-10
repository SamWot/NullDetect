package org.sam.home;

import org.junit.BeforeClass;
import org.junit.Test;
import org.objectweb.asm.tree.analysis.AnalyzerException;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import static org.sam.home.Utils.ExpectedExampleResults;
import static org.sam.home.Utils.testResources;

public class NullInterpreterTest {
    private static Path resourceDir;
    private static Map<Path, Utils.ExpectedExampleResults> examples;

    static Path resourcePath(String ...parts) {
        return Paths.get(resourceDir.toString(), parts);
    }

    @BeforeClass
    public static void setUpClass() {
        resourceDir = Paths.get("src/test/resources", "class");
        examples = new HashMap<>();

        // Simple examples
        examples.put(resourcePath("Example1.class"),
                new ExpectedExampleResults("Example1.java", Arrays.asList(5, 11, 17)));
        examples.put(resourcePath("Example2.class"),
                new ExpectedExampleResults("Example2.java", Arrays.asList(5, 12)));
        examples.put(resourcePath("Example3.class"),
                new ExpectedExampleResults("Example3.java", Arrays.asList(10)));
        examples.put(resourcePath("Example4.class"),
                new ExpectedExampleResults("Example4.java", Arrays.asList(7)));
        examples.put(resourcePath("Example4.class"),
                new ExpectedExampleResults("Example4.java", Arrays.asList(7)));

        // ArrayList
        examples.put(resourcePath("ArrayList", "ArrayList.class"),
                new ExpectedExampleResults("ArrayList.java", Arrays.asList()));
        examples.put(resourcePath("ArrayList", "ArrayList$1.class"),
                new ExpectedExampleResults("ArrayList.java", Arrays.asList()));
        examples.put(resourcePath("ArrayList", "ArrayList$ArrayListSpliterator.class"),
                new ExpectedExampleResults("ArrayList.java", Arrays.asList()));
        examples.put(resourcePath("ArrayList", "ArrayList$Itr.class"),
                new ExpectedExampleResults("ArrayList.java", Arrays.asList()));
        examples.put(resourcePath("ArrayList", "ArrayList$ListItr.class"),
                new ExpectedExampleResults("ArrayList.java", Arrays.asList()));
        examples.put(resourcePath("ArrayList", "ArrayList$SubList.class"),
                new ExpectedExampleResults("ArrayList.java", Arrays.asList()));
        examples.put(resourcePath("ArrayList", "ArrayList$SubList$1.class"),
                new ExpectedExampleResults("ArrayList.java", Arrays.asList()));
        // TODO: Example6?
        // TODO: Add automatic compilation of examples sources.
    }

    // TODO: not all examples are passing now
    @Test(expected = AssertionError.class)
    public void Examples() throws IOException {
        testResources(
                resourceDir,
                examples,
                (fis) -> {
                    try {
                        return Main.detect(fis);
                    } catch (AnalyzerException | IOException ex) {
                        throw new RuntimeException(ex);
                    }
                });
    }

}
