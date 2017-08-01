package org.sam.home;

import org.junit.Before;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.Type;
import org.objectweb.asm.tree.*;
import org.junit.Test;

public class NullCompareInstTest {
    private ClassNode cn;
    private MethodNode mn;
    private LabelNode labeln;
    private LineNumberNode linen;
    private AbstractInsnNode in1;
    private AbstractInsnNode in2;

    @Before
    public void setUp() {
        this.cn = new ClassNode(Opcodes.ASM5);
        this.cn.version = Opcodes.V1_8;
        this.cn.name = "FooBar";
        this.cn.sourceFile = "FooBar.class";
        this.cn.superName = "java/lang/Object";

        this.mn = new MethodNode(
                Opcodes.ASM5,
                Opcodes.ACC_PUBLIC,
                "buz",
                Type.getMethodDescriptor(Type.VOID_TYPE),
                null,
                null);
        this.mn.instructions = new InsnList();

        this.labeln = new LabelNode();
        this.linen = new LineNumberNode(123, this.labeln);
        this.in1 = new InsnNode(Opcodes.NOP);
        this.in2 = new InsnNode(Opcodes.NOP);
    }


    @Test
    public void CreateValid() {
        this.mn.instructions.add(this.labeln);
        this.mn.instructions.add(this.linen);
        this.mn.instructions.add(this.in1);
        this.mn.instructions.add(this.in2);
        this.cn.methods.add(this.mn);
        NullCompareInst compareInst = new NullCompareInst(this.cn, this.mn, this.in1);
    }
}
