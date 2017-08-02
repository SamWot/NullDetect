package org.sam.home;

import org.junit.Assert;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.Type;
import org.objectweb.asm.tree.*;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.*;
import java.util.*;
import java.util.stream.Stream;

public class PotentialComparesTest {
    private static Path resourceDir;
    private static Map<String, ExampleResults> examples;

    private static class ExampleResults {
        public String sourceFileName;
        public List<Integer> lineNumbers;

        public ExampleResults(final String sourceFileName, final List<Integer> lineNumbers) {
            this.sourceFileName = sourceFileName;
            this.lineNumbers = lineNumbers;
        }
    }

    @BeforeClass
    public static void setUpClass() {
        resourceDir = Paths.get("src/test/resources", "class");
        examples = new HashMap<>();
        examples.put("Example1.class", new ExampleResults("Example1.java", Arrays.asList(5, 11, 17)));
        examples.put("Example2.class", new ExampleResults("Example2.java", Arrays.asList(4, 5, 12)));
        examples.put("Example3.class", new ExampleResults("Example3.java", Arrays.asList(10)));
        examples.put("Example4.class", new ExampleResults("Example4.java", Arrays.asList(7)));
        examples.put("Example5.class", new ExampleResults("Example5.java", Arrays.asList(5)));
        // TODO: Example6, ArrayList?
        // TODO: Add automatic compilation of examples sources.
    }

    @Test
    public void ArtificialExample() {
        ClassNode cn = new ClassNode(Opcodes.ASM5);
        cn.version = Opcodes.V1_8;
        cn.name = "FooBar";
        cn.sourceFile = "FooBar.java";
        cn.superName = "java/lang/Object";

        MethodNode mn = new MethodNode(
                Opcodes.ASM5,
                Opcodes.ACC_PUBLIC,
                "buz",
                Type.getMethodDescriptor(Type.VOID_TYPE),
                null,
                null);
        cn.methods.add(mn);
        mn.instructions = new InsnList();

        LabelNode startLabel = new LabelNode();
        LabelNode endLabel = new LabelNode();

        mn.instructions.add(startLabel);
        mn.instructions.add(new LineNumberNode(123, startLabel));
        mn.instructions.add(new InsnNode(Opcodes.NOP));
        mn.instructions.add(new JumpInsnNode(Opcodes.IFNULL, endLabel));
        mn.instructions.add(new InsnNode(Opcodes.NOP));
        mn.instructions.add(new InsnNode(Opcodes.NOP));
        mn.instructions.add(new LineNumberNode(456, startLabel));
        mn.instructions.add(new InsnNode(Opcodes.NOP));
        mn.instructions.add(new InsnNode(Opcodes.NOP));
        mn.instructions.add(new JumpInsnNode(Opcodes.IFNONNULL, endLabel));
        mn.instructions.add(new InsnNode(Opcodes.NOP));
        mn.instructions.add(endLabel);

        List<NullCompareInst> potentialCompares = Main.findPotentialCompares(cn);
        Assert.assertEquals(2, potentialCompares.size());
        Assert.assertEquals(123, potentialCompares.get(0).lineNumber());
        Assert.assertEquals("FooBar.java", potentialCompares.get(0).sourceFileName());
        Assert.assertEquals(456, potentialCompares.get(1).lineNumber());
        Assert.assertEquals("FooBar.java", potentialCompares.get(1).sourceFileName());
    }

    private static List<NullCompareInst> findPotentials(final InputStream fis) throws IOException {
        final ClassReader cr = new ClassReader(fis);
        final ClassNode cn = new ClassNode(Opcodes.ASM5);
        cr.accept(cn, 0);
        return Main.findPotentialCompares(cn);
    }

    private static boolean validateInstList(final List<NullCompareInst> insts,
                                            final String sourceFileName,
                                            final List<Integer> lineNumbers) {
        if (insts.size() != lineNumbers.size()) {
            return false;
        }
        lineNumbers.sort(Comparator.comparingInt(Integer::intValue));
        insts.sort(Comparator.comparingInt(NullCompareInst::lineNumber));
        for (int i = 0; i < insts.size(); i++) {
            final NullCompareInst inst = insts.get(i);
            if (inst.lineNumber() != lineNumbers.get(i) ||
                    !inst.sourceFileName().equals(sourceFileName)) {
                return false;
            }
        }
        return true;
    }

    @Test
    public void Examples() throws IOException {
        try (final Stream<Path> stream = Files.list(resourceDir)) {
            stream.forEach(path -> {
                String fileName = path.getFileName().toString();
                if (examples.containsKey(fileName)) {
                    ExampleResults expected = examples.get(fileName);
                    try (final InputStream fis = Files.newInputStream(path, StandardOpenOption.READ)) {
                        final List<NullCompareInst> insts = findPotentials(fis);
                        Assert.assertTrue(validateInstList(insts, expected.sourceFileName, expected.lineNumbers));
                    } catch (IOException ex) {
                        Assert.assertTrue(false);
                    }
                }
            });
        }
    }
}
