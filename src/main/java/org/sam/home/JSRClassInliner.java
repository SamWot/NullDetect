package org.sam.home;

import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.commons.JSRInlinerAdapter;
import org.objectweb.asm.tree.ClassNode;

class JSRClassInliner extends ClassNode {

    public JSRClassInliner(final int api) {
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
}
