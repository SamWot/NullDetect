package org.sam.home.ui;

import javafx.application.Platform;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.tree.MethodInsnNode;
import org.objectweb.asm.tree.MethodNode;
import org.objectweb.asm.tree.analysis.AnalyzerException;
import org.sam.home.Utils;
import org.sam.home.analyzer.NullAnalyzer;
import org.sam.home.analyzer.NullClassNode;
import org.sam.home.analyzer.NullValue;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import java.util.concurrent.*;

import static org.sam.home.Utils.testResources;
import static org.sam.home.Utils.testResourcesPath;

public class AnalyzerJobTest {
    private static Path resourceDir;
    private static Map<Path, Utils.ExpectedExampleResults> examplesPassing;
    private static Map<Path, Utils.ExpectedExampleResults> examplesFailling;


    static Path resourcePath(String ...parts) {
        return Paths.get(resourceDir.toString(), parts);
    }

    @BeforeClass
    public static void setUpClass() {
        resourceDir = Paths.get("src/test/resources", "class");
        examplesPassing = new HashMap<>();
        examplesFailling = new HashMap<>();

        // Simple examples
        examplesPassing.put(resourcePath("Example1.class"),
                new Utils.ExpectedExampleResults("Example1.java", Arrays.asList(5, 11, 17)));
        examplesPassing.put(resourcePath("Example4.class"),
                new Utils.ExpectedExampleResults("Example4.java", Arrays.asList(7)));
        examplesPassing.put(resourcePath("Example5.class"),
                new Utils.ExpectedExampleResults("Example5.java", Arrays.asList(5)));
        examplesPassing.put(resourcePath("ExampleStaticMethod.class"),
                new Utils.ExpectedExampleResults("ExampleStaticMethod.java", Arrays.asList(17)));
        examplesPassing.put(resourcePath("ExampleVirtualMethod.class"),
                new Utils.ExpectedExampleResults("ExampleVirtualMethod.java", Arrays.asList(23)));
        examplesPassing.put(resourcePath("ExampleVirtualMethod2.class"),
                new Utils.ExpectedExampleResults("ExampleVirtualMethod2.java", Arrays.asList(11)));
        examplesPassing.put(resourcePath("ExamplePrivateMethod.class"),
                new Utils.ExpectedExampleResults("ExamplePrivateMethod.java", Arrays.asList(9)));
        // currently failing:
        examplesFailling.put(resourcePath("Example2.class"),
                new Utils.ExpectedExampleResults("Example2.java", Arrays.asList(5, 12)));
        examplesFailling.put(resourcePath("Example3.class"),
                new Utils.ExpectedExampleResults("Example3.java", Arrays.asList(10)));


        // ArrayList
        examplesPassing.put(resourcePath("ArrayList", "ArrayList.class"),
                new Utils.ExpectedExampleResults("ArrayList.java", Arrays.asList()));
        examplesPassing.put(resourcePath("ArrayList", "ArrayList$1.class"),
                new Utils.ExpectedExampleResults("ArrayList.java", Arrays.asList()));
        examplesPassing.put(resourcePath("ArrayList", "ArrayList$ArrayListSpliterator.class"),
                new Utils.ExpectedExampleResults("ArrayList.java", Arrays.asList()));
        examplesPassing.put(resourcePath("ArrayList", "ArrayList$Itr.class"),
                new Utils.ExpectedExampleResults("ArrayList.java", Arrays.asList()));
        examplesPassing.put(resourcePath("ArrayList", "ArrayList$ListItr.class"),
                new Utils.ExpectedExampleResults("ArrayList.java", Arrays.asList()));
        examplesPassing.put(resourcePath("ArrayList", "ArrayList$SubList.class"),
                new Utils.ExpectedExampleResults("ArrayList.java", Arrays.asList()));
        examplesPassing.put(resourcePath("ArrayList", "ArrayList$SubList$1.class"),
                new Utils.ExpectedExampleResults("ArrayList.java", Arrays.asList()));
    }

    // FIXME: these are same tests as in analyzer/NullInterpreterTest. Maybe move them there?
    @Test
    public void examplesPass() throws IOException {
        testResourcesPath(
                resourceDir,
                examplesPassing,
                path -> {
                    final ExecutorService executor = Executors.newSingleThreadExecutor();
                    final CompletionService<AnalyzerJob.AnalyzerResult> completionService =
                            new ExecutorCompletionService<>(executor);

                    try {
                        Future<AnalyzerJob.AnalyzerResult> res = completionService.submit(new AnalyzerJob(path));
                        return res.get().getRedundantInsts();
                    } catch (InterruptedException | ExecutionException ex) {
                        throw new RuntimeException(ex);
                    } finally {
                        executor.shutdownNow();
                    }
                });
    }

    @Test(expected = AssertionError.class)
    public void examplesFail() throws IOException {
        testResourcesPath(
                resourceDir,
                examplesFailling,
                path -> {
                    final ExecutorService executor = Executors.newSingleThreadExecutor();
                    final CompletionService<AnalyzerJob.AnalyzerResult> completionService =
                            new ExecutorCompletionService<>(executor);

                    try {
                        Future<AnalyzerJob.AnalyzerResult> res = completionService.submit(new AnalyzerJob(path));
                        return res.get().getRedundantInsts();
                    } catch (InterruptedException | ExecutionException ex) {
                        throw new RuntimeException(ex);
                    } finally {
                        executor.shutdownNow();
                    }
                });
    }
}
