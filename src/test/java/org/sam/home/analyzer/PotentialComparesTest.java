package org.sam.home.analyzer;

import org.junit.Assert;
import org.junit.Test;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.Type;
import org.objectweb.asm.tree.*;
import org.sam.home.ExpectedResults;

import java.io.IOException;
import java.util.List;

import static org.sam.home.Utils.testResources;

public class PotentialComparesTest {
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
                ExpectedResults.RESOURCES_DIR,
                ExpectedResults.POTENTIAL,
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
