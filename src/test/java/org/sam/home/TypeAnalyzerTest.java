package org.sam.home;

import org.junit.BeforeClass;
import org.objectweb.asm.tree.analysis.AnalyzerException;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.Type;
import org.objectweb.asm.tree.*;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Iterator;
import java.util.Optional;

public class TypeAnalyzerTest {
    private static Path resource;
    private static TypeAnalyzer analyzer;
    private static ClassNode cn;

    @BeforeClass
    public static void setUpClass() throws IOException {
        final Path resourceDir = Paths.get("src/test/resources", "class");
        resource = Utils.findResourceFile(resourceDir, "TypeAnalyzerTest.class");
        cn = Utils.nodeForResource(resource);
        analyzer = new TypeAnalyzer(cn);
    }

    private static Optional<MethodNode> findMethod(String name) {
        for (MethodNode m: cn.methods) {
            if (m.name.equals(name)) {
                return Optional.of(m);
            }
        }
        return Optional.empty();
    }

    private static Optional<AbstractInsnNode> findInst(MethodNode method, int opcode) {
        for (Iterator<AbstractInsnNode> it = method.instructions.iterator(); it.hasNext();) {
            AbstractInsnNode inst = it.next();
            if (inst.getOpcode() == opcode) {
                return Optional.of(inst);
            }
        }
        return Optional.empty();
    }

    @Test
    public void ldcString() throws AnalyzerException {
        // String foo = "foo";
        MethodNode method = findMethod("ldcString").get();
        AbstractInsnNode ldc = findInst(method, Opcodes.LDC).get();
        Type type = analyzer.topStackType(method, ldc.getNext());
        Assert.assertEquals(Type.getType(String.class), type);
    }

    @Test
    public void ldcObject() throws AnalyzerException {
        // Object foo = "foo";
        MethodNode method = findMethod("ldcObject").get();
        AbstractInsnNode ldc = findInst(method, Opcodes.LDC).get();
        Type type = analyzer.topStackType(method, ldc.getNext());
        Assert.assertEquals(Type.getType(String.class), type);
    }

    @Test
    public void newString() throws AnalyzerException {
        // Object foo = new String();
        MethodNode method = findMethod("newString").get();
        AbstractInsnNode newInst = findInst(method, Opcodes.NEW).get();
        Type type = analyzer.topStackType(method, newInst.getNext());
        Assert.assertEquals(Type.getType(String.class), type);
    }

    @Test
    public void checkcast() throws AnalyzerException {
        // Object foo = "foo";
        // String bar = (String) foo;
        MethodNode method = findMethod("checkcast").get();
        AbstractInsnNode checkcast = findInst(method, Opcodes.CHECKCAST).get();
        Type type = analyzer.topStackType(method, checkcast.getNext());
        Assert.assertEquals(Type.getType(String.class), type);
    }

    @Test
    public void invoke() throws AnalyzerException {
        // Object foo = String.join("foo", "bar");
        MethodNode method = findMethod("invoke").get();
        AbstractInsnNode invoke = findInst(method, Opcodes.INVOKESTATIC).get();
        Type type = analyzer.topStackType(method, invoke.getNext());
        Assert.assertEquals(Type.getType(String.class), type);
    }

}
