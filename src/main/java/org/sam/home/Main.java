package org.sam.home;

import org.objectweb.asm.*;
import org.objectweb.asm.tree.AbstractInsnNode;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.MethodNode;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
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

        for (String fileName: args) {
            try (final FileInputStream fis = new FileInputStream(fileName)) {
                final List<NullCompareInst> instrs;
                try {
                    instrs = detect(fis);
                } catch (IOException ex) {
                    System.err.println("Can't read file: " + fileName);
                    continue;
                }

                for (NullCompareInst instr: instrs) {
                    System.out.println(instr.lineInfo());
                }
            } catch (FileNotFoundException ex) {
                System.err.println("No such file: " + fileName);
            } catch (IOException ex) {
                System.err.println("Something gone wrong during file close: " + fileName);
                ex.printStackTrace();
            }
        }
    }

    public static List<NullCompareInst> detect(final FileInputStream fis) throws IOException {
        final ClassReader cr;
        try {
            cr = new ClassReader(fis);
        } catch (IOException ex) {
            throw new IOException("Can't read file");
        }

        final ClassNode cn = new ClassNode(Opcodes.ASM5);
        cr.accept(cn, 0);

        return potentialCompares(cn);
    }

    public static List<NullCompareInst> potentialCompares(final ClassNode cn) {
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
