package org.sam.home;

import org.objectweb.asm.tree.analysis.Analyzer;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.tree.AbstractInsnNode;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.MethodNode;
import org.objectweb.asm.tree.analysis.AnalyzerException;
import org.objectweb.asm.tree.analysis.Frame;

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
                System.out.println("In class " + fileName + " found " + insts.size() + " redundant null checks:");
                for (final NullCompareInst inst: insts) {
                    System.out.println(inst.lineInfo());
                }
                System.out.println();
            } catch (IOException | SecurityException | UnsupportedOperationException ex) {
                System.err.println("Can't read file: " + fileName);
                continue;
            } catch (AnalyzerException ex) {
                System.err.println("Something went wrong during analysis:");
                System.err.println(ex.getMessage());
                ex.printStackTrace();
                continue;
            }
        }
    }

    public static List<NullCompareInst> detect(final InputStream fis) throws IOException, AnalyzerException {
        final ClassReader cr;
        cr = new ClassReader(fis);

        final ClassNode cn = new JSRClassInliner(Opcodes.ASM5);
        cr.accept(cn, 0);

        return filterRedundant(findPotentialCompares(cn));
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

    public static List<NullCompareInst> filterRedundant(final List<NullCompareInst> compares) throws AnalyzerException {
        final List<NullCompareInst> redundant = new ArrayList<>();

        final Analyzer<NullValue> nullAnalyzer = new Analyzer<>(new NullInterpreter(Opcodes.ASM5));
        for (final NullCompareInst compare: compares) {
            // TODO: merge different NullCompareInstructions from same method, to minimize calls to analyze()
            final Frame<NullValue>[] frames =
                    nullAnalyzer.analyze(compare.getClassNode().name, compare.getMethodNode());
            final Frame<NullValue> frame = frames[compare.instIndex()];
            if (frame == null) {
                continue;
            }
            final NullValue nullValue = frame.getStack(compare.getStackOperandIdx());
            if (nullValue == NullValue.NULL || nullValue == NullValue.NOTNULL) {
                redundant.add(compare);
            }
        }

        return redundant;
    }
}
