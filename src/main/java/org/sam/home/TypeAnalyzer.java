package org.sam.home;

import org.objectweb.asm.Opcodes;
import org.objectweb.asm.Type;
import org.objectweb.asm.tree.AbstractInsnNode;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.MethodNode;
import org.objectweb.asm.tree.analysis.*;

public class TypeAnalyzer extends Analyzer<BasicValue> {
    private ClassNode cn;

    private TypeAnalyzer(TypeInterpreter interpreter, ClassNode cn) {
        super(interpreter);
        this.cn = cn;
    }

    public TypeAnalyzer(ClassNode cn) {
        this(new TypeInterpreter(), cn);
    }

    public ClassNode getClassNode() {
        return cn;
    }

    public Type topStackType(final MethodNode method, final AbstractInsnNode inst) throws AnalyzerException {
        if (!method.instructions.contains(inst)) {
            throw new IllegalArgumentException("Instruction not in method");
        }

        final Frame<BasicValue>[] frames = this.analyze(this.cn.name, method);
        final Frame<BasicValue> frame = frames[method.instructions.indexOf(inst)];
        if (frame == null || frame.getStackSize() == 0) {
            return Type.VOID_TYPE;
        }
        return frame.getStack(0).getType();
    }

    private static class TypeInterpreter extends BasicInterpreter {
        @Override
        public BasicValue newValue(final Type type) {
            final BasicValue value = super.newValue(type);
            if (value != null && value.equals(BasicValue.REFERENCE_VALUE)) {
                return new BasicValue(type);
            }
            return value;
        }
    }

}
