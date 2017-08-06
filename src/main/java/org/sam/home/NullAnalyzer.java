package org.sam.home;

import org.objectweb.asm.Opcodes;
import org.objectweb.asm.tree.AbstractInsnNode;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.MethodNode;
import org.objectweb.asm.tree.analysis.Analyzer;
import org.objectweb.asm.tree.analysis.AnalyzerException;
import org.objectweb.asm.tree.analysis.Frame;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class NullAnalyzer extends Analyzer<NullValue> {
    private NullInterpreter interpreter;

    private NullAnalyzer(NullInterpreter interpreter) {
        super(interpreter);
        this.interpreter = interpreter;
    }

    public NullAnalyzer() {
        this(new NullInterpreter(Opcodes.ASM5));
    }

    public List<NullCompareInst> findPotentialCompares(final ClassNode cn) {
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
                        // E.g. ACONST_NULL might not be on top of stack:
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

    public List<NullCompareInst> filterRedundant(final List<NullCompareInst> compares) throws AnalyzerException {
        final List<NullCompareInst> redundant = new ArrayList<>();

        for (final NullCompareInst compare: compares) {
            // TODO: merge different NullCompareInstructions from same method, to minimize calls to analyze()
            // TODO; or cache results from previous analyses
            final Frame<NullValue>[] frames =
                    this.analyze(compare.getClassNode().name, compare.getMethodNode());
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
