package org.sam.home;

import org.objectweb.asm.tree.*;

public final class NullCompareInst {
    private final ClassNode cn;
    private final MethodNode mn;
    private final AbstractInsnNode in;

    public NullCompareInst(ClassNode cn, MethodNode mn, AbstractInsnNode in) {
        if (!mn.instructions.contains(in)) {
            throw new IllegalArgumentException("Instruction not in method");
        }
        if (cn.sourceFile == null) {
            throw new IllegalArgumentException("No debug info for class " + cn.name);
        }

        this.cn = cn;
        this.mn = mn;
        this.in = in;
    }

    public ClassNode getClassNode() {
        return this.cn;
    }

    public MethodNode getMethodNode() {
        return this.mn;
    }

    public AbstractInsnNode getInstructionNode() {
        return this.in;
    }

    @Override
    public String toString() {
        return this.cn.name + "." + this.mn.name + " " + this.mn.instructions.indexOf(this.in);
    }

    public int lineNumber() {
        AbstractInsnNode inst = this.in;
        do {
            inst = inst.getPrevious();
        } while (inst != null && inst.getType() != AbstractInsnNode.LINE);
        if (inst == null) {
            throw new IllegalStateException("No debug info for class " + this.cn.name);
        }
        return inst == null ? 0 : ((LineNumberNode)inst).line;
    }

    public String sourceFileName() {
        if (this.cn.sourceFile == null) {
            throw new IllegalStateException("No debug info for class " + this.cn.name);
        }
        return this.cn.sourceFile;
    }

    public String lineInfo() {
        return this.sourceFileName() + ":" + Integer.toString(this.lineNumber());
    }
}
