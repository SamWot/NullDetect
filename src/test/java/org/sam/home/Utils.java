package org.sam.home;

import org.junit.Assert;

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

    static class ExpectedExampleResults {
        public String sourceFileName;
        public List<Integer> lineNumbers;

        public ExpectedExampleResults(final String sourceFileName, final List<Integer> lineNumbers) {
            this.sourceFileName = sourceFileName;
            this.lineNumbers = lineNumbers;
        }
    }

    static void testResources(final Path resourceDir,
                              final Map<String, ExpectedExampleResults> expectedResults,
                              final Function<InputStream, List<NullCompareInst>> testFunction)
            throws IOException {
        try (final Stream<Path> stream = Files.list(resourceDir)) {
            stream.forEach(path -> {
                String fileName = path.getFileName().toString();
                if (expectedResults.containsKey(fileName)) {
                    ExpectedExampleResults expected = expectedResults.get(fileName);
                    try (final InputStream fis = Files.newInputStream(path, StandardOpenOption.READ)) {
                        final List<NullCompareInst> insts = testFunction.apply(fis);
                        Assert.assertTrue("Testing " + fileName,
                                Utils.validateInstList(insts,
                                        expected.sourceFileName,
                                        expected.lineNumbers));
                    } catch (IOException ex) {
                        Assert.assertTrue(false);
                    }
                }
            });
        }
    }

    static boolean validateInstList(final List<NullCompareInst> insts,
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

    static Path findResourceFile(final Path resourceDir, final String fileName) throws IOException {
        final PathMatcher nameFilter = resourceDir.getFileSystem().
                getPathMatcher("glob:**" + fileName);
        try (final Stream<Path> stream = Files.list(resourceDir)) {
            return stream.filter(nameFilter::matches).findFirst().get();
        }
    }
}
