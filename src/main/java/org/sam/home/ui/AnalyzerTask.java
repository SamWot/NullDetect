package org.sam.home.ui;

import javafx.application.Platform;
import javafx.beans.property.ReadOnlyObjectProperty;
import javafx.beans.property.ReadOnlyObjectWrapper;
import javafx.collections.FXCollections;
import javafx.collections.ObservableMap;
import javafx.concurrent.Task;
import org.sam.home.analyzer.NullCompareInst;

import java.io.IOException;
import java.io.InterruptedIOException;
import java.nio.file.*;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.*;
import java.util.stream.Stream;

/**
 * Task that runs AnalyzerJobs in background threads via ThreadExecutorPool.
 * This task exposes ObservableMap which is populated analysis results from different AnalyzerJobs.
 */
final class AnalyzerTask extends Task<ObservableMap<Path, List<NullCompareInst>>> {
    // Number of background working threads = all available cores.
    private static final int NUM_THREADS = Runtime.getRuntime().availableProcessors() + 1;

    // File matcher that filters only class-files
    private static final PathMatcher CLASS_MATCHER = FileSystems.getDefault().getPathMatcher("glob:**.class");

    private final Path analysisDir;
    private final ReadOnlyObjectWrapper<ObservableMap<Path, List<NullCompareInst>>> analysisResults;


    public AnalyzerTask(final Path analysisDir) {
        this.analysisDir = analysisDir;
        this.analysisResults = new ReadOnlyObjectWrapper<>(
                this,
                "analysisResults",
                FXCollections.<Path, List<NullCompareInst>>observableHashMap());

    }

    private final ObservableMap<Path, List<NullCompareInst>> getAnalysisResults() {
        return this.analysisResults.get();
    }

    public final ReadOnlyObjectProperty<ObservableMap<Path, List<NullCompareInst>>> analysisResultsProperty() {
        return this.analysisResults.getReadOnlyProperty();
    }

    public final Path getAnalysisDir() {
        return this.analysisDir;
    }

    /**
     * Walk over analysis directory and analyze each found class-file. Results are placed in analysis results
     * ObservableMap <p>
     *
     * @throws IOException       if can't find analysis directory
     * @throws SecurityException if can't access analysis directory
     */
    @Override
    protected final ObservableMap<Path, List<NullCompareInst>> call() throws IOException, SecurityException {
        final ExecutorService executor = Executors.newFixedThreadPool(NUM_THREADS);
        final CompletionService<AnalyzerJob.AnalyzerResult> completionService =
                new ExecutorCompletionService<>(executor);

        try {
            long filesNumber = this.createJobs(completionService);
            for (int i = 0; i < filesNumber; i++) {
                // Check if task was canceled then wait for next job
                if (this.isCancelled()) {
                    return this.getAnalysisResults();
                }
                Optional<AnalyzerJob.AnalyzerResult> result = this.waitJob(completionService);
                result.ifPresent(res -> Platform.runLater(
                        () -> this.getAnalysisResults().put(res.getFile(), res.getRedundantInsts())
                ));
                this.updateProgress(i + 1, filesNumber);
            }
        } catch (InterruptedException ex) {
            // Task was canceled
            return this.getAnalysisResults();
        } finally {
            // Shutdown executor to guaranty release of resources
            executor.shutdownNow();
        }

        return this.getAnalysisResults();
    }

    /**
     * Walk over analysis directory. Filter all class-files.
     * Then for each class-file create new AnalyzerJob and submit it for execution
     *
     * @return number of jobs created
     */
    private final long createJobs(CompletionService<AnalyzerJob.AnalyzerResult> completionService)
            throws IOException, SecurityException {
        ClassFileVisitor classVisitor = new ClassFileVisitor(completionService);
        Files.walkFileTree(this.analysisDir, classVisitor);
        if (this.isCancelled())
            return 0;
        return classVisitor.getFilesCount();
    }

    /**
     * Wait for completion of AnalyzerJobs.
     * As individual jobs finish put its results in analysisResults map
     *
     * @return analysis result for individual job
     */
    private final Optional<AnalyzerJob.AnalyzerResult> waitJob(
            CompletionService<AnalyzerJob.AnalyzerResult> completionService) throws InterruptedException {
        try {
            Future<AnalyzerJob.AnalyzerResult> future = completionService.take();
            AnalyzerJob.AnalyzerResult result = future.get();
            return Optional.of(result);
        } catch (ExecutionException ex) {
            // FIXME: For now simply suppress ExecutionException if something went wrong during analysis of
            // individual file
            System.err.println("Something went wrong during analyze");
            ex.printStackTrace();
        } catch (InterruptedException ex) {
            if (isCancelled()) {
                throw ex;
            }
        }
        return Optional.empty();
    }

    /**
     * FileVisitor that walks over all class-files in directory and creates new AnalyzerJob for each
     */
    private class ClassFileVisitor implements FileVisitor<Path> {
        private long filesCount = 0;
        private final CompletionService<AnalyzerJob.AnalyzerResult> completionService;

        public ClassFileVisitor(CompletionService<AnalyzerJob.AnalyzerResult> completionService) {
            this.completionService = completionService;
        }

        @Override
        public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
            if (AnalyzerTask.this.isCancelled())
                return FileVisitResult.TERMINATE;

            if (!attrs.isRegularFile())
                return FileVisitResult.CONTINUE;

            if (!CLASS_MATCHER.matches(file))
                return FileVisitResult.CONTINUE;

            this.completionService.submit(new AnalyzerJob(file));
            this.filesCount++;
            return FileVisitResult.CONTINUE;
        }

        @Override
        public FileVisitResult visitFileFailed(Path file, IOException exc) throws IOException {
            if (AnalyzerTask.this.isCancelled())
                return FileVisitResult.TERMINATE;
            return FileVisitResult.CONTINUE;
        }

        @Override
        public FileVisitResult postVisitDirectory(Path dir, IOException exc) throws IOException {
            if (AnalyzerTask.this.isCancelled())
                return FileVisitResult.TERMINATE;
            return FileVisitResult.CONTINUE;
        }

        @Override
        public FileVisitResult preVisitDirectory(Path dir, BasicFileAttributes attrs) throws IOException {
            if (AnalyzerTask.this.isCancelled())
                return FileVisitResult.TERMINATE;
            return FileVisitResult.CONTINUE;
        }

        public long getFilesCount() {
            return this.filesCount;
        }
    }
}
