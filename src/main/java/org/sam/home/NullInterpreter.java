package org.sam.home;

import org.objectweb.asm.Opcodes;
import org.objectweb.asm.Type;
import org.objectweb.asm.tree.AbstractInsnNode;
import org.objectweb.asm.tree.FieldInsnNode;
import org.objectweb.asm.tree.InvokeDynamicInsnNode;
import org.objectweb.asm.tree.MethodInsnNode;
import org.objectweb.asm.tree.analysis.AnalyzerException;
import org.objectweb.asm.tree.analysis.Interpreter;

import java.util.List;

public class NullInterpreter extends Interpreter<NullValue> {
    public NullInterpreter(final int api) {
        super(api);
    }

    @Override
    public NullValue newValue(Type type) {
        // TODO: need to track primitive vs. reference types in NullValue
        // TODO: need to track exact value for primitive types for better analysis
        if (type == null) {
            return NullValue.MAYBENULL;
        }
        if (type.getSort() == Type.ARRAY || type.getSort() == Type.OBJECT) {
            return NullValue.MAYBENULL;
        }
        return NullValue.NOTNULL;
    }

    @Override
    public NullValue newOperation(AbstractInsnNode insn) throws AnalyzerException {
        switch (insn.getOpcode()) {
            case Opcodes.ACONST_NULL:
                return NullValue.NULL;
            case Opcodes.ICONST_M1:
            case Opcodes.ICONST_0:
            case Opcodes.ICONST_1:
            case Opcodes.ICONST_2:
            case Opcodes.ICONST_3:
            case Opcodes.ICONST_4:
            case Opcodes.ICONST_5:
            case Opcodes.LCONST_0:
            case Opcodes.LCONST_1:
            case Opcodes.FCONST_0:
            case Opcodes.FCONST_1:
            case Opcodes.FCONST_2:
            case Opcodes.DCONST_0:
            case Opcodes.DCONST_1:
            case Opcodes.BIPUSH:
            case Opcodes.SIPUSH:
            case Opcodes.LDC:
                return NullValue.NOTNULL;
            case Opcodes.GETSTATIC: {
                Type resType = Type.getType(((FieldInsnNode) insn).desc);
                if (resType.getSort() == Type.ARRAY || resType.getSort() == Type.OBJECT) {
                    // TODO: try to check value of this static field.
                    return NullValue.MAYBENULL;
                } else {
                    // TODO: mb trace value of this primitive static field
                    return NullValue.NOTNULL;
                }
            }
            case Opcodes.NEW:
                return NullValue.NOTNULL;
            case Opcodes.JSR:
                throw new AnalyzerException(insn, "JSR should have been eliminated earlier");
            default:
                throw new AnalyzerException(insn, "Invalid instruction in newOperation()");
        }
    }

    @Override
    public NullValue copyOperation(AbstractInsnNode insn, NullValue value) throws AnalyzerException {
        return value;
    }

    @Override
    public NullValue unaryOperation(AbstractInsnNode insn, NullValue value) throws AnalyzerException {
        switch (insn.getOpcode()) {
            case Opcodes.IFNONNULL:
            case Opcodes.IFNULL:
            case Opcodes.IFEQ:
            case Opcodes.IFNE:
            case Opcodes.IFLT:
            case Opcodes.IFGE:
            case Opcodes.IFGT:
            case Opcodes.IFLE:
            case Opcodes.TABLESWITCH:
            case Opcodes.LOOKUPSWITCH:
                // TODO: try to check if branch is taken (maybe enhance Analyzer<> class)
                return null;
            case Opcodes.IRETURN:
            case Opcodes.LRETURN:
            case Opcodes.FRETURN:
            case Opcodes.DRETURN:
            case Opcodes.ARETURN:
            case Opcodes.ATHROW:
            case Opcodes.MONITORENTER:
            case Opcodes.MONITOREXIT:
                return null;
            case Opcodes.INEG:
            case Opcodes.LNEG:
            case Opcodes.FNEG:
            case Opcodes.DNEG:
            case Opcodes.IINC:
            case Opcodes.I2L:
            case Opcodes.I2F:
            case Opcodes.I2D:
            case Opcodes.L2I:
            case Opcodes.L2F:
            case Opcodes.L2D:
            case Opcodes.F2I:
            case Opcodes.F2L:
            case Opcodes.F2D:
            case Opcodes.D2I:
            case Opcodes.D2L:
            case Opcodes.D2F:
            case Opcodes.I2B:
            case Opcodes.I2C:
            case Opcodes.I2S:
                return value;
            case Opcodes.PUTSTATIC:
                // TODO: try to check this static value.
                return null;
            case Opcodes.GETFIELD:{
                Type resType = Type.getType(((FieldInsnNode) insn).desc);
                if (resType.getSort() == Type.ARRAY || resType.getSort() == Type.OBJECT) {
                    // TODO: try to check value of this field.
                    return NullValue.MAYBENULL;
                } else {
                    // TODO: mb trace value of this primitive field
                    return NullValue.NOTNULL;
                }
            }
            case Opcodes.NEWARRAY:
            case Opcodes.ANEWARRAY:
            case Opcodes.ARRAYLENGTH:
                return NullValue.NOTNULL;
            case Opcodes.CHECKCAST:
                return value; // checkcast either doesn't change change stack or throw exception
            case Opcodes.INSTANCEOF:
                return NullValue.NOTNULL; // instanceof always return either 0 or 1
            default:
                throw new AnalyzerException(insn, "Invalid instruction in unaryOperation()");
        }
    }

    @Override
    public NullValue binaryOperation(AbstractInsnNode insn, NullValue value1, NullValue value2)
            throws AnalyzerException {
        switch (insn.getOpcode()) {
            case Opcodes.IALOAD:
            case Opcodes.LALOAD:
            case Opcodes.FALOAD:
            case Opcodes.DALOAD:
            case Opcodes.BALOAD:
            case Opcodes.CALOAD:
            case Opcodes.SALOAD:
            case Opcodes.IADD:
            case Opcodes.LADD:
            case Opcodes.FADD:
            case Opcodes.DADD:
            case Opcodes.ISUB:
            case Opcodes.LSUB:
            case Opcodes.FSUB:
            case Opcodes.DSUB:
            case Opcodes.IMUL:
            case Opcodes.LMUL:
            case Opcodes.FMUL:
            case Opcodes.DMUL:
            case Opcodes.IDIV:
            case Opcodes.LDIV:
            case Opcodes.FDIV:
            case Opcodes.DDIV:
            case Opcodes.IREM:
            case Opcodes.LREM:
            case Opcodes.FREM:
            case Opcodes.DREM:
            case Opcodes.ISHL:
            case Opcodes.LSHL:
            case Opcodes.ISHR:
            case Opcodes.LSHR:
            case Opcodes.IUSHR:
            case Opcodes.LUSHR:
            case Opcodes.IAND:
            case Opcodes.LAND:
            case Opcodes.IOR:
            case Opcodes.LOR:
            case Opcodes.IXOR:
            case Opcodes.LXOR:
            case Opcodes.LCMP:
            case Opcodes.FCMPL:
            case Opcodes.FCMPG:
            case Opcodes.DCMPL:
            case Opcodes.DCMPG:
                return NullValue.NOTNULL;
            case Opcodes.IF_ICMPEQ:
            case Opcodes.IF_ICMPNE:
            case Opcodes.IF_ICMPLT:
            case Opcodes.IF_ICMPGE:
            case Opcodes.IF_ICMPGT:
            case Opcodes.IF_ICMPLE:
            case Opcodes.IF_ACMPEQ:
            case Opcodes.IF_ACMPNE:
                // TODO: try to check if branch is taken (maybe enhance Analyzer<> class)
                return null;
            case Opcodes.PUTFIELD:
                // TODO: try to check this field
                return null;
            case Opcodes.AALOAD:
                // TODO: track values in the array
                return NullValue.MAYBENULL;
            default:
                throw new AnalyzerException(insn, "Invalid instruction in binaryOperation()");
        }
    }

    @Override
    public NullValue ternaryOperation(AbstractInsnNode insn, NullValue value1, NullValue value2, NullValue value3)
            throws AnalyzerException {
        switch (insn.getOpcode()) {
            case Opcodes.IASTORE:
            case Opcodes.LASTORE:
            case Opcodes.FASTORE:
            case Opcodes.DASTORE:
            case Opcodes.AASTORE:
            case Opcodes.BASTORE:
            case Opcodes.CASTORE:
            case Opcodes.SASTORE:
                // TODO: track values in the array
                return null;
            default:
                throw new AnalyzerException(insn, "Invalid instruction in ternaryOperation()");
        }
    }

    @Override
    public NullValue naryOperation(AbstractInsnNode insn, List<? extends NullValue> values) throws AnalyzerException {
        switch (insn.getOpcode()) {
            case Opcodes.INVOKEVIRTUAL:
            case Opcodes.INVOKESPECIAL:
            case Opcodes.INVOKESTATIC:
            case Opcodes.INVOKEINTERFACE: {
                Type resType = Type.getReturnType(((MethodInsnNode) insn).desc);
                if (resType.getSort() == Type.ARRAY || resType.getSort() == Type.OBJECT) {
                    // TODO: try to check return value of this method.
                    return NullValue.MAYBENULL;
                } else if (resType.getSort() == Type.VOID)  {
                    return null;
                } else {
                    // TODO: mb try to trace value of this return-value of this method
                    return NullValue.NOTNULL;
                }
            }
            case Opcodes.INVOKEDYNAMIC: {
                Type resType = Type.getReturnType(((InvokeDynamicInsnNode) insn).desc);
                if (resType.getSort() == Type.ARRAY || resType.getSort() == Type.OBJECT) {
                    return NullValue.MAYBENULL;
                } else if (resType.getSort() == Type.VOID)  {
                    return null;
                } else {
                    return NullValue.NOTNULL;
                }
            }
            case Opcodes.MULTIANEWARRAY:
                return NullValue.NOTNULL;
            default:
                throw new AnalyzerException(insn, "Invalid instruction in naryOperation()");
        }
    }

    // TODO
    @Override
    public void returnOperation(AbstractInsnNode insn, NullValue value, NullValue expected) throws AnalyzerException {}

    // TODO
    @Override
    public NullValue merge(NullValue v, NullValue w) {
        if (v == w) {
            return v;
        } else {
            return NullValue.MAYBENULL;
        }
    }
}
