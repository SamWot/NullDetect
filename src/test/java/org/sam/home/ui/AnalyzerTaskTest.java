package org.sam.home.ui;

import org.junit.Assert;
import org.junit.Test;
import org.sam.home.ExpectedResults;
import org.sam.home.analyzer.NullCompareInst;
import org.testfx.framework.junit.ApplicationTest;

import java.nio.file.Path;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.*;

public class AnalyzerTaskTest extends ApplicationTest {
    private static final int TIMEOUT_SECONDS = 10;

    /**
     * Run AnalyzerTask with test resources directory
     */
    @Test
    public void analyzeResourcesDir() throws ExecutionException, InterruptedException {
        final Map<Path, List<NullCompareInst>> expectedResults;

        final ExecutorService executor = Executors.newSingleThreadExecutor();
        final CompletionService<Map<Path, List<NullCompareInst>>> completionService =
                new ExecutorCompletionService<>(executor);

        AnalyzerTask task = new AnalyzerTask(ExpectedResults.RESOURCES_DIR);
        interact((Callable<Object>)
                () -> {
                    try {
                        completionService.submit(task, new HashMap<>());
                        // limit waiting to 10 seconds - this should more than enough for correct analysis
                        Future<Map<Path, List<NullCompareInst>>> res = completionService.poll(TIMEOUT_SECONDS, TimeUnit.SECONDS);
                        Assert.assertNotNull("AnalyzerTask should've finished by now", res);
                        Assert.assertNull("No new results should be in queue", completionService.poll());
//                        Assert.assertTrue(task.getState() == Worker.State.SUCCEEDED);
                    } finally {
                        executor.shutdownNow();
                    }
                    return null;

                }
        );
    }
}
