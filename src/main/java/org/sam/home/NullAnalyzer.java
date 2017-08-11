package org.sam.home;

import org.objectweb.asm.Opcodes;
import org.objectweb.asm.Type;
import org.objectweb.asm.commons.Method;
import org.objectweb.asm.tree.AbstractInsnNode;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.MethodNode;
import org.objectweb.asm.tree.analysis.Analyzer;
import org.objectweb.asm.tree.analysis.AnalyzerException;
import org.objectweb.asm.tree.analysis.Frame;

import java.util.*;
import java.util.stream.IntStream;

public class NullAnalyzer extends Analyzer<NullValue> {
    private NullInterpreter interpreter;
    private NullClassNode cn;

    private NullAnalyzer(NullInterpreter interpreter, NullClassNode cn) {
        super(interpreter);
        this.interpreter = interpreter;
        this.cn = cn;
    }

    public NullAnalyzer(NullClassNode cn) {
        this(new NullInterpreter(Opcodes.ASM5), cn);
        this.interpreter.setAnalyzer(this);
    }

    public NullClassNode getClassNode() {
        return cn;
    }

    public Optional<NullValue> analyzeMethodReturnValue(MethodNode method) throws AnalyzerException {
        Method methodDesc = new Method(method.name, method.desc);
        if (methodDesc.getReturnType().getSort() == Type.VOID) {
            return Optional.empty();
        }

        if (methodDesc.getReturnType().getSort() != Type.OBJECT
            && methodDesc.getReturnType().getSort() != Type.ARRAY) {
            return Optional.of(NullValue.NOTNULL);
        }

        // Base Analyzer caches a lot of info in its private fields,
        // so it is required to create new analyzer for every nested method analysis
        final NullAnalyzer analyzer = new NullAnalyzer(this.cn);
        final Frame<NullValue>[] frames = analyzer.analyze(this.cn.name, method);
        final Frame<NullValue> returnFrame =
                IntStream.range(0, method.instructions.size())
                        .filter(i -> method.instructions.get(i).getOpcode() == Opcodes.ARETURN
                                        && frames[i] != null)
                        .mapToObj(i -> frames[i])
                        .reduce(
                                (first, second) -> {
                                    try {
                                        first.merge(second, this.interpreter);
                                        return first;
                                    } catch (AnalyzerException ex) {
                                        throw new IllegalArgumentException(ex.getMessage());
                                    }})
                        .orElseThrow(() -> new AnalyzerException(null, "No ARETURN instruction found"));
        return Optional.of(returnFrame.getStack(0));
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
            // TODO: or cache results from previous analyses
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
