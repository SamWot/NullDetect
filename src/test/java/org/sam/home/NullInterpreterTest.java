package org.sam.home;

import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.tree.MethodInsnNode;
import org.objectweb.asm.tree.MethodNode;
import org.objectweb.asm.tree.analysis.AnalyzerException;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import static org.sam.home.Utils.ExpectedExampleResults;
import static org.sam.home.Utils.testResources;

public class NullInterpreterTest {
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
                new ExpectedExampleResults("Example1.java", Arrays.asList(5, 11, 17)));
        examplesPassing.put(resourcePath("Example4.class"),
                new ExpectedExampleResults("Example4.java", Arrays.asList(7)));
        examplesPassing.put(resourcePath("Example5.class"),
                new ExpectedExampleResults("Example5.java", Arrays.asList(5)));
        examplesPassing.put(resourcePath("ExampleStaticMethod.class"),
                new ExpectedExampleResults("ExampleStaticMethod.java", Arrays.asList(17)));
        examplesPassing.put(resourcePath("ExampleVirtualMethod.class"),
                new ExpectedExampleResults("ExampleVirtualMethod.java", Arrays.asList(23)));
        examplesPassing.put(resourcePath("ExampleVirtualMethod2.class"),
                new ExpectedExampleResults("ExampleVirtualMethod2.java", Arrays.asList(11)));
        // currently failing:
        examplesFailling.put(resourcePath("Example2.class"),
                new ExpectedExampleResults("Example2.java", Arrays.asList(5, 12)));
        examplesFailling.put(resourcePath("Example3.class"),
                new ExpectedExampleResults("Example3.java", Arrays.asList(10)));


        // ArrayList
        examplesPassing.put(resourcePath("ArrayList", "ArrayList.class"),
                new ExpectedExampleResults("ArrayList.java", Arrays.asList()));
        examplesPassing.put(resourcePath("ArrayList", "ArrayList$1.class"),
                new ExpectedExampleResults("ArrayList.java", Arrays.asList()));
        examplesPassing.put(resourcePath("ArrayList", "ArrayList$ArrayListSpliterator.class"),
                new ExpectedExampleResults("ArrayList.java", Arrays.asList()));
        examplesPassing.put(resourcePath("ArrayList", "ArrayList$Itr.class"),
                new ExpectedExampleResults("ArrayList.java", Arrays.asList()));
        examplesPassing.put(resourcePath("ArrayList", "ArrayList$ListItr.class"),
                new ExpectedExampleResults("ArrayList.java", Arrays.asList()));
        examplesPassing.put(resourcePath("ArrayList", "ArrayList$SubList.class"),
                new ExpectedExampleResults("ArrayList.java", Arrays.asList()));
        examplesPassing.put(resourcePath("ArrayList", "ArrayList$SubList$1.class"),
                new ExpectedExampleResults("ArrayList.java", Arrays.asList()));
        // TODO: Example6?
        // TODO: Add automatic compilation of examples sources.
    }

    @Test
    public void examplesPass() throws IOException {
        testResources(
                resourceDir,
                examplesPassing,
                (fis) -> {
                    try {
                        return Main.detect(fis);
                    } catch (AnalyzerException | IOException ex) {
                        throw new RuntimeException(ex);
                    }
                });
    }

    // TODO: not all examples are passing now
    @Test(expected = AssertionError.class)
    public void examplesFail() throws IOException {
        testResources(
                resourceDir,
                examplesFailling,
                (fis) -> {
                    try {
                        return Main.detect(fis);
                    } catch (AnalyzerException | IOException ex) {
                        throw new RuntimeException(ex);
                    }
                });
    }

    @Test
    public void analyzeStaticMethodReturnValue() throws IOException, AnalyzerException {
        Path resource = Utils.findResourceFile(resourceDir, "ExampleStaticMethod.class");
        final NullClassNode cn = Utils.nodeForResource(resource);

        final MethodNode foo = cn.tryResolveStatic(
                new MethodInsnNode(Opcodes.INVOKESTATIC,
                        "test/ExampleStaticMethod",
                        "foo",
                        "()Ljava/lang/Object;",
                        false))
                .get();

        final MethodNode fooInt = cn.tryResolveStatic(
                new MethodInsnNode(Opcodes.INVOKESTATIC,
                        "test/ExampleStaticMethod",
                        "foo",
                        "(I)Ljava/lang/Object;",
                        false))
                .get();


        final MethodNode bar = cn.tryResolveStatic(
                new MethodInsnNode(Opcodes.INVOKESTATIC,
                        "test/ExampleStaticMethod",
                        "bar",
                        "()Ljava/lang/String;",
                        false))
                .get();

        MethodNode test1 = null;
        for (final MethodNode method: cn.methods) {
            if (method.name.equals("test1")) {
                test1 = method;
                break;
            }
        }

        final NullAnalyzer analyzer = new NullAnalyzer(cn);
        Optional<NullValue> returnValue = analyzer.analyzeMethodReturnValue(foo);
        Assert.assertTrue(returnValue.isPresent());
        Assert.assertEquals(NullValue.NOTNULL, returnValue.get());

        returnValue = analyzer.analyzeMethodReturnValue(fooInt);
        Assert.assertTrue(returnValue.isPresent());
        Assert.assertEquals(NullValue.NULL, returnValue.get());

        returnValue = analyzer.analyzeMethodReturnValue(bar);
        Assert.assertTrue(returnValue.isPresent());
        Assert.assertEquals(NullValue.NULL, returnValue.get());

        returnValue = analyzer.analyzeMethodReturnValue(test1);
        Assert.assertFalse(returnValue.isPresent());
    }

}
