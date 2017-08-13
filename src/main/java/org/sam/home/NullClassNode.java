package org.sam.home;

import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.Type;
import org.objectweb.asm.commons.JSRInlinerAdapter;
import org.objectweb.asm.commons.Method;
import org.objectweb.asm.tree.AbstractInsnNode;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.MethodInsnNode;
import org.objectweb.asm.tree.MethodNode;
import org.objectweb.asm.tree.analysis.AnalyzerException;

import java.util.Optional;

public class NullClassNode extends ClassNode {

    public NullClassNode(final int api) {
        super(api);
    }

    @Override
    public MethodVisitor visitMethod(int access, String name, String desc, String signature, String[] exceptions) {
        return new JSRInlinerAdapter(
                super.visitMethod(access, name, desc, signature, exceptions),
                access,
                name,
                desc,
                signature,
                exceptions);
    }

    public Optional<MethodNode> findMethodByInst(final AbstractInsnNode inst) {
        for (MethodNode method: this.methods) {
            if (method.instructions.contains(inst)) {
                return Optional.of(method);
            }
        }
        return Optional.empty();
    }

    public Optional<MethodNode> tryResolveStatic(final MethodInsnNode invoke) {
        if (invoke.getOpcode() != Opcodes.INVOKESTATIC) {
            return Optional.empty();
        }
        if (invoke.owner == null || !invoke.owner.equals(this.name)) {
            // owner should be this class or we have no access to its body
            return Optional.empty();
        }
        if (invoke.name.equals("<init>") || invoke.name.equals("<clinit>")) {
            // this shouldn't be constructor or class initializer
            return Optional.empty();
        }
        Method invokeDesc = new Method(invoke.name, invoke.desc);
        for (MethodNode method: this.methods) {
            if (((method.access & Opcodes.ACC_STATIC) == 0) // not static method
                || ((method.access & Opcodes.ACC_NATIVE) != 0) // native method)
                || ((method.access & Opcodes.ACC_ABSTRACT) != 0)) { // abstract method
                continue;
            }

            if (method.desc.equals(invokeDesc.getDescriptor())
                    && method.name.equals(invokeDesc.getName())) {
                return Optional.of(method);
            }
        }
        return Optional.empty();
    }

    public Optional<MethodNode> tryResolveVirtual(final MethodInsnNode invoke) throws AnalyzerException {
        if (invoke.getOpcode() != Opcodes.INVOKEVIRTUAL) {
            return Optional.empty();
        }
        if (invoke.name.equals("<init>") || invoke.name.equals("<clinit>")) {
            // this shouldn't be constructor or class initializer
            return Optional.empty();
        }
        Method invokeDesc = new Method(invoke.name, invoke.desc);
        TypeAnalyzer analyzer = new TypeAnalyzer(this);
        Optional<MethodNode> thisMethod = findMethodByInst(invoke);
        if (!thisMethod.isPresent()) {
            return Optional.empty();
        }
        Type top = analyzer.topStackType(thisMethod.get(), invoke);
        if (!top.equals(Type.getObjectType(this.name))) {
            // can't resolve method with differentrun-time type
            return Optional.empty();
        }
        for (MethodNode method: this.methods) {
            if (((method.access & Opcodes.ACC_FINAL) == 0) // not final method
                && ((this.access & Opcodes.ACC_FINAL) == 0)) { // not final class
                continue;
            }

            if (((method.access & Opcodes.ACC_STATIC) != 0) // static method
                || ((method.access & Opcodes.ACC_NATIVE) != 0) // native method
                || ((method.access & Opcodes.ACC_PRIVATE) != 0) // private method
                || ((method.access & Opcodes.ACC_ABSTRACT) != 0)) { // abstract method
                continue;
            }

            if (method.desc.equals(invokeDesc.getDescriptor())
                    && method.name.equals(invokeDesc.getName())) {
                return Optional.of(method);
            }
        }
        return Optional.empty();
    }

    public Optional<MethodNode> tryResolveMethod(final MethodInsnNode invoke) throws AnalyzerException {
        if (invoke.getOpcode() == Opcodes.INVOKESTATIC) {
            return tryResolveStatic(invoke);
        } else if (invoke.getOpcode() == Opcodes.INVOKEVIRTUAL) {
            return tryResolveVirtual(invoke);
        }
        return Optional.empty();
    }
}
