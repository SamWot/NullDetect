package org.sam.home;

import org.objectweb.asm.Opcodes;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.commons.JSRInlinerAdapter;
import org.objectweb.asm.commons.Method;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.MethodInsnNode;
import org.objectweb.asm.tree.MethodNode;

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

            if (method.desc.equals(invokeDesc.getDescriptor()) && method.name.equals(invokeDesc.getName())) {
                return Optional.of(method);
            }
        }
        return Optional.empty();
    }
}
