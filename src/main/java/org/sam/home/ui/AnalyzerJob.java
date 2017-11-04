package org.sam.home.ui;

import org.objectweb.asm.tree.analysis.AnalyzerException;
import org.sam.home.analyzer.NullAnalyzer;
import org.sam.home.analyzer.NullCompareInst;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.List;
import java.util.concurrent.Callable;

/**
 * Callable that executes null-analysis for single file and returns its result
 */
class AnalyzerJob implements Callable<AnalyzerJob.AnalyzerResult> {
    private final Path file;

    public AnalyzerJob(final Path file) {
        this.file = file;
    }

    @Override
    public AnalyzerResult call() throws IOException, SecurityException, UnsupportedOperationException, AnalyzerException {
        try (final InputStream fis = Files.newInputStream(this.file, StandardOpenOption.READ)) {
            return new AnalyzerResult(file, NullAnalyzer.findRedundantNullChecks(fis));
        }
    }

    static class AnalyzerResult {
        private final Path file;
        private final List<NullCompareInst> redundantInsts;

        public AnalyzerResult(Path file, List<NullCompareInst> redundantInsts) {
            this.file = file;
            this.redundantInsts = redundantInsts;
        }

        public Path getFile() {
            return file;
        }

        public List<NullCompareInst> getRedundantInsts() {
            return redundantInsts;
        }
    }
}
