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
    private AbstractInsnNode ifnonnull;
    private AbstractInsnNode ifnull;

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
        this.ifnonnull = new JumpInsnNode(Opcodes.IFNONNULL, labeln);
        this.ifnull = new JumpInsnNode(Opcodes.IFNULL, labeln);
    }


    @Test
    public void createValid() {
        this.mn.instructions.add(this.labeln);
        this.mn.instructions.add(this.linen);
        this.mn.instructions.add(this.ifnonnull);
        this.mn.instructions.add(this.ifnull);
        this.cn.methods.add(this.mn);
        new NullCompareInst(this.cn, this.mn, this.ifnull);
    }

    @Test(expected = IllegalArgumentException.class)
    public void createNoDebugInfo() {
        this.cn.name = null;
        this.cn.sourceFile = null;
        this.mn.instructions.add(this.ifnonnull);
        this.cn.methods.add(this.mn);
        new NullCompareInst(this.cn, this.mn, this.ifnull);
    }

    @Test(expected = IllegalArgumentException.class)
    public void createNoInstruction() {
        this.mn.instructions.add(this.labeln);
        this.mn.instructions.add(this.linen);
        this.mn.instructions.add(this.ifnonnull);
        this.cn.methods.add(this.mn);
        new NullCompareInst(this.cn, this.mn, this.ifnull);
    }

    @Test(expected = IllegalArgumentException.class)
    public void createInvalidInstruction() {
        AbstractInsnNode nop = new InsnNode(Opcodes.NOP);
        this.mn.instructions.add(this.labeln);
        this.mn.instructions.add(this.linen);
        this.mn.instructions.add(nop);
        this.cn.methods.add(this.mn);
        new NullCompareInst(this.cn, this.mn, nop);
    }

    @Test
    public void debugInfo1() {
        this.mn.instructions.add(this.labeln);
        this.mn.instructions.add(this.linen);
        this.mn.instructions.add(this.ifnonnull);
        this.mn.instructions.add(this.ifnull);
        this.cn.methods.add(this.mn);
        NullCompareInst nc1Inst = new NullCompareInst(this.cn, this.mn, this.ifnonnull);
        Assert.assertEquals(123, nc1Inst.lineNumber());
        Assert.assertEquals("FooBar.java", nc1Inst.sourceFileName());
        Assert.assertEquals("FooBar.java:123", nc1Inst.lineInfo());
        NullCompareInst nc2Inst = new NullCompareInst(this.cn, this.mn, this.ifnull);
        Assert.assertEquals(123, nc2Inst.lineNumber());
        Assert.assertEquals("FooBar.java", nc2Inst.sourceFileName());
        Assert.assertEquals("FooBar.java:123", nc2Inst.lineInfo());
    }

    @Test
    public void debugInfo2() {
        this.mn.instructions.add(this.labeln);
        this.mn.instructions.add(this.linen);
        this.mn.instructions.add(new LineNumberNode(456, this.labeln));
        this.mn.instructions.add(this.ifnonnull);
        this.mn.instructions.add(this.ifnull);
        this.cn.methods.add(this.mn);
        NullCompareInst ncInst = new NullCompareInst(this.cn, this.mn, this.ifnonnull);
        Assert.assertEquals(456, ncInst.lineNumber());
        Assert.assertEquals("FooBar.java", ncInst.sourceFileName());
        Assert.assertEquals("FooBar.java:456", ncInst.lineInfo());
    }

    @Test
    public void debugInfo3() {
        this.mn.instructions.add(this.labeln);
        this.mn.instructions.add(this.linen);
        this.mn.instructions.add(this.ifnonnull);
        this.mn.instructions.add(new LineNumberNode(456, this.labeln));
        this.mn.instructions.add(this.ifnull);
        this.cn.methods.add(this.mn);
        NullCompareInst nc1Inst = new NullCompareInst(this.cn, this.mn, this.ifnonnull);
        Assert.assertEquals(123, nc1Inst.lineNumber());
        Assert.assertEquals("FooBar.java", nc1Inst.sourceFileName());
        Assert.assertEquals("FooBar.java:123", nc1Inst.lineInfo());
        NullCompareInst nc2Inst = new NullCompareInst(this.cn, this.mn, this.ifnull);
        Assert.assertEquals(456, nc2Inst.lineNumber());
        Assert.assertEquals("FooBar.java", nc2Inst.sourceFileName());
        Assert.assertEquals("FooBar.java:456", nc2Inst.lineInfo());
    }

    @Test(expected = IllegalStateException.class)
    public void noSourceFileName() {
        this.mn.instructions.add(this.labeln);
        this.mn.instructions.add(this.linen);
        this.mn.instructions.add(this.ifnonnull);
        this.mn.instructions.add(this.ifnull);
        this.cn.methods.add(this.mn);
        NullCompareInst ncInst = new NullCompareInst(this.cn, this.mn, this.ifnonnull);
        ncInst.getClassNode().sourceFile = null;
        ncInst.sourceFileName();
    }

    @Test(expected = IllegalStateException.class)
    public void noLineNumber1() {
        this.mn.instructions.add(this.labeln);
        this.mn.instructions.add(this.ifnonnull);
        this.mn.instructions.add(this.ifnull);
        this.cn.methods.add(this.mn);
        NullCompareInst ncInst = new NullCompareInst(this.cn, this.mn, this.ifnonnull);
        ncInst.lineNumber();
    }

    @Test(expected = IllegalStateException.class)
    public void noLineNumber2() {
        this.mn.instructions.add(this.labeln);
        this.mn.instructions.add(this.ifnonnull);
        this.mn.instructions.add(this.ifnull);
        this.mn.instructions.add(this.linen);
        this.cn.methods.add(this.mn);
        NullCompareInst ncInst = new NullCompareInst(this.cn, this.mn, this.ifnonnull);
        ncInst.lineNumber();
    }
}
