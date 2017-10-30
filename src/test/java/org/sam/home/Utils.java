package org.sam.home;

import org.junit.Assert;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.Opcodes;
import org.sam.home.analyzer.NullClassNode;
import org.sam.home.analyzer.NullCompareInst;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.PathMatcher;
import java.nio.file.StandardOpenOption;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Stream;

public final class Utils {
    private Utils() {}

    public static class ExpectedExampleResults {
        public String sourceFileName;
        public List<Integer> lineNumbers;

        public ExpectedExampleResults(final String sourceFileName, final List<Integer> lineNumbers) {
            this.sourceFileName = sourceFileName;
            this.lineNumbers = lineNumbers;
        }
    }

    public static void testResources(final Path resourceDir,
                              final Map<Path, ExpectedExampleResults> expectedResults,
                              final Function<InputStream, List<NullCompareInst>> testFunction)
            throws IOException {
        try (final Stream<Path> stream = Files.walk(resourceDir)) {
            stream.forEach(path -> {
                if (expectedResults.containsKey(path)) {
                    ExpectedExampleResults expected = expectedResults.get(path);
                    try (final InputStream fis = Files.newInputStream(path, StandardOpenOption.READ)) {
                        final List<NullCompareInst> insts = testFunction.apply(fis);
                        Assert.assertTrue("Testing " + path,
                                Utils.validateInstList(
                                        insts,
                                        expected.sourceFileName,
                                        expected.lineNumbers));
                    } catch (IOException ex) {
                        Assert.assertTrue(false);
                    }
                }
            });
        }
    }

    public static boolean validateInstList(final List<NullCompareInst> insts,
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

    public static Path findResourceFile(final Path resourceDir, final String fileName) throws IOException {
        final PathMatcher nameFilter = resourceDir.getFileSystem().
                getPathMatcher("glob:**" + fileName);
        try (final Stream<Path> stream = Files.list(resourceDir)) {
            return stream.filter(nameFilter::matches).findFirst().get();
        }
    }

    public static NullClassNode nodeForResource(Path resourceFile) throws IOException {
        try (final InputStream fis = Files.newInputStream(resourceFile, StandardOpenOption.READ)) {
            final ClassReader cr = new ClassReader(fis);
            final NullClassNode cn = new NullClassNode(Opcodes.ASM5);
            cr.accept(cn, 0);
            return cn;
        }
    }
}
