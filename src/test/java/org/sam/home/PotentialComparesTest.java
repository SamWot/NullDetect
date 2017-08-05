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
    private static Map<String, Utils.ExpectedExampleResults> examples;

    @BeforeClass
    public static void setUpClass() {
        resourceDir = Paths.get("src/test/resources", "class");
        examples = new HashMap<>();
        examples.put("Example1.class",
                new ExpectedExampleResults("Example1.java", Arrays.asList(5, 11, 17)));
        examples.put("Example2.class",
                new ExpectedExampleResults("Example2.java", Arrays.asList(4, 5, 12)));
        examples.put("Example3.class",
                new ExpectedExampleResults("Example3.java", Arrays.asList(10)));
        examples.put("Example4.class",
                new ExpectedExampleResults("Example4.java", Arrays.asList(7)));
        examples.put("Example5.class",
                new ExpectedExampleResults("Example5.java", Arrays.asList(5)));
        // TODO: Example6, ArrayList?
        // TODO: Add automatic compilation of examples sources.
    }

    @Test
    public void ArtificialExample() {
        ClassNode cn = new ClassNode(Opcodes.ASM5);
        cn.version = Opcodes.V1_8;
        cn.name = "FooBar";
        cn.sourceFile = "FooBar.java";
        cn.superName = "java/lang/Object";

        MethodNode mn = new MethodNode(
                Opcodes.ASM5,
                Opcodes.ACC_PUBLIC,
                "buz",
                Type.getMethodDescriptor(Type.VOID_TYPE),
                null,
                null);
        cn.methods.add(mn);
        mn.instructions = new InsnList();

        LabelNode startLabel = new LabelNode();
        LabelNode endLabel = new LabelNode();

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

        List<NullCompareInst> potentialCompares = Main.findPotentialCompares(cn);
        Assert.assertEquals(2, potentialCompares.size());
        Assert.assertEquals(123, potentialCompares.get(0).lineNumber());
        Assert.assertEquals("FooBar.java", potentialCompares.get(0).sourceFileName());
        Assert.assertEquals(456, potentialCompares.get(1).lineNumber());
        Assert.assertEquals("FooBar.java", potentialCompares.get(1).sourceFileName());
    }

    @Test
    public void Examples() throws IOException {
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
                    final ClassNode cn = new ClassNode(Opcodes.ASM5);
                    cr.accept(cn, 0);
                    return Main.findPotentialCompares(cn);
                });
    }
}
