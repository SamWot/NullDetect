package org.sam.home.analyzer;

import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.Type;
import org.objectweb.asm.tree.*;
import org.sam.home.Utils;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Iterator;
import java.util.Optional;

public class NullClassNodeTest {
    private static Path resourceDir;

    @BeforeClass
    public static void setUpClass() {
        resourceDir = Paths.get("src/test/resources", "class");
    }

    @Test
    public void inline() {
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

        LabelNode subLabel = new LabelNode();

        mn.instructions.add(new JumpInsnNode(Opcodes.JSR, subLabel));
        mn.instructions.add(new InsnNode(Opcodes.RETURN));
        mn.instructions.add(subLabel);
        mn.instructions.add(new VarInsnNode(Opcodes.ASTORE, 1));
        mn.instructions.add(new IincInsnNode(0, 1));
        mn.instructions.add(new VarInsnNode(Opcodes.RET, 1));

        ClassNode adapted = new NullClassNode(Opcodes.ASM5);
        cn.accept(adapted);

        Assert.assertEquals(1, adapted.methods.size());
        InsnList adaptedInsts = adapted.methods.get(0).instructions;
        for (Iterator<AbstractInsnNode> i = adaptedInsts.iterator(); i.hasNext();) {
            final AbstractInsnNode inst = i.next();
            Assert.assertNotEquals(Opcodes.JSR, inst.getOpcode());
            Assert.assertNotEquals(Opcodes.RET, inst.getOpcode());
        }
    }

    @Test
    public void tryResolveStatic() throws IOException {
        Path resource = Utils.findResourceFile(resourceDir, "ExampleStaticMethod.class");
        final NullClassNode cn = Utils.nodeForResource(resource);
        Optional<MethodNode> mn = cn.tryResolveStatic(
                new MethodInsnNode(Opcodes.INVOKEVIRTUAL,   // wrong opcode
                        "test/ExampleStaticMethod",
                        "foo",
                        "()Ljava/lang/Object;",
                        false));
        Assert.assertFalse(mn.isPresent());

        mn = cn.tryResolveStatic(
                new MethodInsnNode(Opcodes.INVOKESTATIC,
                        "test/ExampleStaticMethod",
                        "foo",
                        "()Ljava/lang/Object;",
                        false));
        Assert.assertTrue(mn.isPresent());
        Assert.assertEquals("foo", mn.get().name);
        Assert.assertEquals("()Ljava/lang/Object;", mn.get().desc);

        mn = cn.tryResolveStatic(
                new MethodInsnNode(Opcodes.INVOKESTATIC,
                        "test/ExampleStaticMethod",
                        "foo",
                        "(I)Ljava/lang/Object;",
                        false));
        Assert.assertTrue(mn.isPresent());
        Assert.assertEquals("foo", mn.get().name);
        Assert.assertEquals("(I)Ljava/lang/Object;", mn.get().desc);

        mn = cn.tryResolveStatic(
                new MethodInsnNode(Opcodes.INVOKESTATIC,
                        "test/ExampleStaticMethod",
                        "bar",                          // wrong name
                        "()Ljava/lang/Object;",
                        false));
        Assert.assertFalse(mn.isPresent());

        mn = cn.tryResolveStatic(
                new MethodInsnNode(Opcodes.INVOKESTATIC,
                        "test/ExampleStaticMethod",
                        "foo",
                        "()Ljava/lang/String;", // wrong return descriptor
                        false));
        Assert.assertFalse(mn.isPresent());
    }
}
