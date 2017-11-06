package org.sam.home.ui;

import org.junit.Test;
import org.sam.home.ExpectedResults;

import java.io.IOException;
import java.util.concurrent.*;

import static org.sam.home.Utils.testResourcesPath;

public class AnalyzerJobTest {
    // FIXME: these are same tests as in analyzer/NullInterpreterTest. Maybe move them there?
    @Test
    public void examplesPass() throws IOException {
        testResourcesPath(
                ExpectedResults.RESOURCES_DIR,
                ExpectedResults.REDUNDANT_PASSING,
                path -> {
                    final ExecutorService executor = Executors.newSingleThreadExecutor();
                    final CompletionService<AnalyzerJob.AnalyzerResult> completionService =
                            new ExecutorCompletionService<>(executor);

                    try {
                        Future<AnalyzerJob.AnalyzerResult> res = completionService.submit(new AnalyzerJob(path));
                        return res.get().getRedundantInsts();
                    } catch (InterruptedException | ExecutionException ex) {
                        throw new RuntimeException(ex);
                    } finally {
                        executor.shutdownNow();
                    }
                });
    }

    @Test(expected = AssertionError.class)
    public void examplesFail() throws IOException {
        testResourcesPath(
                ExpectedResults.RESOURCES_DIR,
                ExpectedResults.REDUNDANT_FAILING,
                path -> {
                    final ExecutorService executor = Executors.newSingleThreadExecutor();
                    final CompletionService<AnalyzerJob.AnalyzerResult> completionService =
                            new ExecutorCompletionService<>(executor);

                    try {
                        Future<AnalyzerJob.AnalyzerResult> res = completionService.submit(new AnalyzerJob(path));
                        return res.get().getRedundantInsts();
                    } catch (InterruptedException | ExecutionException ex) {
                        throw new RuntimeException(ex);
                    } finally {
                        executor.shutdownNow();
                    }
                });
    }
}
