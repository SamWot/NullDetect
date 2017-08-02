package org.sam.home;

import org.objectweb.asm.*;
import org.objectweb.asm.tree.AbstractInsnNode;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.MethodNode;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.*;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * Main class
 */
public class Main {
    public static void main(String[] args) {
        if (args.length == 0) {
            System.err.println("No file name specified");
            return;
        }

        for (final String fileName: args) {
            final Path file;
            try {
                file = Paths.get(fileName);
            } catch (InvalidPathException ex) {
                System.err.println("No such file: " + fileName);
                continue;
            }

            try (final InputStream fis = Files.newInputStream(file, StandardOpenOption.READ)) {
                final List<NullCompareInst> insts = detect(fis);
                for (final NullCompareInst inst: insts) {
                    System.out.println(inst.lineInfo());
                }
            } catch (IOException | SecurityException | UnsupportedOperationException ex) {
                System.err.println("Can't read file: " + fileName);
                continue;
            }
        }
    }

    public static List<NullCompareInst> detect(final InputStream fis) throws IOException {
        final ClassReader cr;
        try {
            cr = new ClassReader(fis);
        } catch (IOException ex) {
            throw new IOException("Can't read file");
        }

        final ClassNode cn = new ClassNode(Opcodes.ASM5);
        cr.accept(cn, 0);

        return findPotentialCompares(cn);
    }

    public static List<NullCompareInst> findPotentialCompares(final ClassNode cn) {
        final List<NullCompareInst> potentialInstrs = new ArrayList<>();
        for (final MethodNode method: cn.methods) {
            for (Iterator<AbstractInsnNode> i = method.instructions.iterator(); i.hasNext();) {
                final AbstractInsnNode inst = i.next();
                switch (inst.getOpcode()) {
                    case Opcodes.IFNONNULL:
                    case Opcodes.IFNULL:
                        potentialInstrs.add(new NullCompareInst(cn, method, inst));
                        break;

                    case Opcodes.IF_ACMPEQ:
                    case Opcodes.IF_ACMPNE:
                        // TODO: Need elaborate logic to find such compares
                        // Problem that ASM is placing pseudo instructions in instructions list (LineNumberNode,
                        // FrameNode).
                        // Also need some complex logic, not just check previous instruction.
                        // E.g. ACONST_NULL might be instruction before previous:
                        // ACONST_NULL
                        // INVOKESPECIAL ...
                        // IF_ACMPEQ ...
                        // Or it can be placed on stack much earlier.
                        break;
                }
            }
        }
        return potentialInstrs;
    }
}
