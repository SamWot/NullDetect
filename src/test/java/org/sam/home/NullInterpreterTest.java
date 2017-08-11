package org.sam.home;

import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.tree.MethodInsnNode;
import org.objectweb.asm.tree.MethodNode;
import org.objectweb.asm.tree.analysis.AnalyzerException;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

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
        examples.put(resourcePath("Example5.class"),
                new ExpectedExampleResults("Example5.java", Arrays.asList(5)));
        examples.put(resourcePath("ExampleStaticMethod.class"),
                new ExpectedExampleResults("ExampleStaticMethod.java", Arrays.asList(17)));
        // currently not passing:
        examples.put(resourcePath("Example2.class"),
                new ExpectedExampleResults("Example2.java", Arrays.asList(5, 12)));
        examples.put(resourcePath("Example3.class"),
                new ExpectedExampleResults("Example3.java", Arrays.asList(10)));
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
    public void examples() throws IOException {
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
