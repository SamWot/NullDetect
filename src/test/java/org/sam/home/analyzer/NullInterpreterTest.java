package org.sam.home.analyzer;

import org.junit.Assert;
import org.junit.Test;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.tree.MethodInsnNode;
import org.objectweb.asm.tree.MethodNode;
import org.objectweb.asm.tree.analysis.AnalyzerException;
import org.sam.home.ExpectedResults;
import org.sam.home.Utils;

import java.io.IOException;
import java.nio.file.Path;
import java.util.Optional;

import static org.sam.home.Utils.testResources;

public class NullInterpreterTest {
    @Test
    public void examplesPass() throws IOException {
        testResources(
                ExpectedResults.RESOURCES_DIR,
                ExpectedResults.REDUNDANT_PASSING,
                (fis) -> {
                    try {
                        return NullAnalyzer.findRedundantNullChecks(fis);
                    } catch (AnalyzerException | IOException ex) {
                        throw new RuntimeException(ex);
                    }
                });
    }

    // TODO: not all examples are passing now
    @Test(expected = AssertionError.class)
    public void examplesFail() throws IOException {
        testResources(
                ExpectedResults.RESOURCES_DIR,
                ExpectedResults.REDUNDANT_FAILING,
                (fis) -> {
                    try {
                        return NullAnalyzer.findRedundantNullChecks(fis);
                    } catch (AnalyzerException | IOException ex) {
                        throw new RuntimeException(ex);
                    }
                });
    }

    @Test
    public void analyzeStaticMethodReturnValue() throws IOException, AnalyzerException {
        Path resource = Utils.findResourceFile(ExpectedResults.RESOURCES_DIR, "ExampleStaticMethod.class");
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
