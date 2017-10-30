package org.sam.home.analyzer;

import org.objectweb.asm.ClassReader;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.tree.analysis.AnalyzerException;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.*;
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

        final NullClassNode cn = new NullClassNode(Opcodes.ASM5);
        cr.accept(cn, 0);

        final NullAnalyzer analyzer = new NullAnalyzer(cn);
        return analyzer.filterRedundant(analyzer.findPotentialCompares(cn));
    }
}
