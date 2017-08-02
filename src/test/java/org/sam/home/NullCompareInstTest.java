package org.sam.home;

import org.junit.Assert;
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
        this.cn.sourceFile = "FooBar.java";
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
        new NullCompareInst(this.cn, this.mn, this.in1);
    }

    @Test(expected = IllegalArgumentException.class)
    public void CreateNoDebugInfo() {
        this.cn.name = null;
        this.cn.sourceFile = null;
        this.mn.instructions.add(this.in1);
        this.cn.methods.add(this.mn);
        new NullCompareInst(this.cn, this.mn, this.in1);
    }

    @Test(expected = IllegalArgumentException.class)
    public void CreateNoInstruction() {
        this.mn.instructions.add(this.labeln);
        this.mn.instructions.add(this.linen);
        this.mn.instructions.add(this.in1);
        this.cn.methods.add(this.mn);
        new NullCompareInst(this.cn, this.mn, this.in2);
    }

    @Test
    public void DebugInfo1() {
        this.mn.instructions.add(this.labeln);
        this.mn.instructions.add(this.linen);
        this.mn.instructions.add(this.in1);
        this.mn.instructions.add(this.in2);
        this.cn.methods.add(this.mn);
        NullCompareInst nc1Inst = new NullCompareInst(this.cn, this.mn, this.in1);
        Assert.assertEquals(123, nc1Inst.lineNumber());
        Assert.assertEquals("FooBar.java", nc1Inst.sourceFileName());
        Assert.assertEquals("FooBar.java:123", nc1Inst.lineInfo());
        NullCompareInst nc2Inst = new NullCompareInst(this.cn, this.mn, this.in2);
        Assert.assertEquals(123, nc2Inst.lineNumber());
        Assert.assertEquals("FooBar.java", nc2Inst.sourceFileName());
        Assert.assertEquals("FooBar.java:123", nc2Inst.lineInfo());
    }

    @Test
    public void DebugInfo2() {
        this.mn.instructions.add(this.labeln);
        this.mn.instructions.add(this.linen);
        this.mn.instructions.add(new LineNumberNode(456, this.labeln));
        this.mn.instructions.add(this.in1);
        this.mn.instructions.add(this.in2);
        this.cn.methods.add(this.mn);
        NullCompareInst ncInst = new NullCompareInst(this.cn, this.mn, this.in1);
        Assert.assertEquals(456, ncInst.lineNumber());
        Assert.assertEquals("FooBar.java", ncInst.sourceFileName());
        Assert.assertEquals("FooBar.java:456", ncInst.lineInfo());
    }

    @Test
    public void DebugInfo3() {
        this.mn.instructions.add(this.labeln);
        this.mn.instructions.add(this.linen);
        this.mn.instructions.add(this.in1);
        this.mn.instructions.add(new LineNumberNode(456, this.labeln));
        this.mn.instructions.add(this.in2);
        this.cn.methods.add(this.mn);
        NullCompareInst nc1Inst = new NullCompareInst(this.cn, this.mn, this.in1);
        Assert.assertEquals(123, nc1Inst.lineNumber());
        Assert.assertEquals("FooBar.java", nc1Inst.sourceFileName());
        Assert.assertEquals("FooBar.java:123", nc1Inst.lineInfo());
        NullCompareInst nc2Inst = new NullCompareInst(this.cn, this.mn, this.in2);
        Assert.assertEquals(456, nc2Inst.lineNumber());
        Assert.assertEquals("FooBar.java", nc2Inst.sourceFileName());
        Assert.assertEquals("FooBar.java:456", nc2Inst.lineInfo());
    }

    @Test(expected = IllegalStateException.class)
    public void NoSourceFileName() {
        this.mn.instructions.add(this.labeln);
        this.mn.instructions.add(this.linen);
        this.mn.instructions.add(this.in1);
        this.mn.instructions.add(this.in2);
        this.cn.methods.add(this.mn);
        NullCompareInst ncInst = new NullCompareInst(this.cn, this.mn, this.in1);
        ncInst.getClassNode().sourceFile = null;
        ncInst.sourceFileName();
    }

    @Test(expected = IllegalStateException.class)
    public void NoLineNumber1() {
        this.mn.instructions.add(this.labeln);
        this.mn.instructions.add(this.in1);
        this.mn.instructions.add(this.in2);
        this.cn.methods.add(this.mn);
        NullCompareInst ncInst = new NullCompareInst(this.cn, this.mn, this.in1);
        ncInst.lineNumber();
    }

    @Test(expected = IllegalStateException.class)
    public void NoLineNumber2() {
        this.mn.instructions.add(this.labeln);
        this.mn.instructions.add(this.in1);
        this.mn.instructions.add(this.in2);
        this.mn.instructions.add(this.linen);
        this.cn.methods.add(this.mn);
        NullCompareInst ncInst = new NullCompareInst(this.cn, this.mn, this.in1);
        ncInst.lineNumber();
    }
}
