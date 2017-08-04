package org.sam.home;

import org.junit.Assert;
import org.junit.Test;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.Type;
import org.objectweb.asm.tree.*;

import java.util.Iterator;

public class JSRCLassInlinerTest {
    @Test
    public void Inline() {
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

        ClassNode adapted = new JSRClassInliner(Opcodes.ASM5);
        cn.accept(adapted);

        Assert.assertEquals(1, adapted.methods.size());
        InsnList adaptedInsts = adapted.methods.get(0).instructions;
        for (Iterator<AbstractInsnNode> i = adaptedInsts.iterator(); i.hasNext();) {
            final AbstractInsnNode inst = i.next();
            Assert.assertNotEquals(Opcodes.JSR, inst.getOpcode());
            Assert.assertNotEquals(Opcodes.RET, inst.getOpcode());
        }
    }
}
