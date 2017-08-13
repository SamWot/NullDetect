package org.sam.home;

import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.Type;
import org.objectweb.asm.tree.*;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.sam.home.Utils.ExpectedExampleResults;
import static org.sam.home.Utils.testResources;

public class PotentialComparesTest {
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
                new ExpectedExampleResults("Example2.java", Arrays.asList(4, 5, 12)));
        examples.put(resourcePath("Example3.class"),
                new ExpectedExampleResults("Example3.java", Arrays.asList(10)));
        examples.put(resourcePath("Example4.class"),
                new ExpectedExampleResults("Example4.java", Arrays.asList(7)));
        examples.put(resourcePath("Example5.class"),
                new ExpectedExampleResults("Example5.java", Arrays.asList(5)));
        examples.put(resourcePath("ExampleStaticMethod.class"),
                new ExpectedExampleResults("ExampleStaticMethod.java", Arrays.asList(17, 23)));
        examples.put(resourcePath("ExampleVirtualMethod.class"),
                new ExpectedExampleResults("ExampleVirtualMethod.java", Arrays.asList(16, 23)));
        examples.put(resourcePath("ExampleVirtualMethod2.class"),
                new ExpectedExampleResults("ExampleVirtualMethod2.java", Arrays.asList(11)));
        examples.put(resourcePath("ExamplePrivateMethod.class"),
                new ExpectedExampleResults("ExamplePrivateMethod.java", Arrays.asList(9)));

        // ArrayList
        examples.put(resourcePath("ArrayList", "ArrayList.class"),
                new ExpectedExampleResults("ArrayList.java", Arrays.asList(311, 313, 331, 333, 520, 522)));
        examples.put(resourcePath("ArrayList", "ArrayList$1.class"),
                new ExpectedExampleResults("ArrayList.java", Arrays.asList()));
        examples.put(resourcePath("ArrayList", "ArrayList$ArrayListSpliterator.class"),
                new ExpectedExampleResults("ArrayList.java", Arrays.asList(1327, 1345, 1362, 1364, 1364)));
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

    @Test
    public void artificialExample() {
        final NullClassNode cn = new NullClassNode(Opcodes.ASM5);
        cn.version = Opcodes.V1_8;
        cn.name = "FooBar";
        cn.sourceFile = "FooBar.java";
        cn.superName = "java/lang/Object";

        final MethodNode mn = new MethodNode(
                Opcodes.ASM5,
                Opcodes.ACC_PUBLIC,
                "buz",
                Type.getMethodDescriptor(Type.VOID_TYPE),
                null,
                null);
        cn.methods.add(mn);
        mn.instructions = new InsnList();

        final LabelNode startLabel = new LabelNode();
        final LabelNode endLabel = new LabelNode();

        mn.instructions.add(startLabel);
        mn.instructions.add(new LineNumberNode(123, startLabel));
        mn.instructions.add(new InsnNode(Opcodes.NOP));
        mn.instructions.add(new JumpInsnNode(Opcodes.IFNULL, endLabel));
        mn.instructions.add(new InsnNode(Opcodes.NOP));
        mn.instructions.add(new InsnNode(Opcodes.NOP));
        mn.instructions.add(new LineNumberNode(456, startLabel));
        mn.instructions.add(new InsnNode(Opcodes.NOP));
        mn.instructions.add(new InsnNode(Opcodes.NOP));
        mn.instructions.add(new JumpInsnNode(Opcodes.IFNONNULL, endLabel));
        mn.instructions.add(new InsnNode(Opcodes.NOP));
        mn.instructions.add(endLabel);

        final NullAnalyzer analyzer = new NullAnalyzer(cn);
        List<NullCompareInst> potentialCompares = analyzer.findPotentialCompares(cn);
        Assert.assertEquals(2, potentialCompares.size());
        Assert.assertEquals(123, potentialCompares.get(0).lineNumber());
        Assert.assertEquals("FooBar.java", potentialCompares.get(0).sourceFileName());
        Assert.assertEquals(456, potentialCompares.get(1).lineNumber());
        Assert.assertEquals("FooBar.java", potentialCompares.get(1).sourceFileName());
    }

    @Test
    public void examples() throws IOException {
        testResources(
                resourceDir,
                examples,
                (fis) -> {
                    final ClassReader cr;
                    try {
                        cr = new ClassReader(fis);
                    } catch (IOException ex) {
                        throw new RuntimeException(ex);
                    }
                    final NullClassNode cn = new NullClassNode(Opcodes.ASM5);
                    cr.accept(cn, 0);

                    final NullAnalyzer analyzer = new NullAnalyzer(cn);
                    return analyzer.findPotentialCompares(cn);
                });
    }
}
