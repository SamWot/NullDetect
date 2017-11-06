package org.sam.home.ui;

import javafx.concurrent.Worker;
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
import java.util.stream.Collectors;

public class AnalyzerTaskTest extends ApplicationTest {
    private static final int TIMEOUT_SECONDS = 10;

    /**
     * Run AnalyzerTask with test resources directory
     */
    @Test
    public void analyzeResourcesDir() throws ExecutionException, InterruptedException {
        final ExecutorService executor = Executors.newSingleThreadExecutor();
        final CompletionService<Map<Path, List<NullCompareInst>>> completionService =
                new ExecutorCompletionService<>(executor);

        AnalyzerTask task = new AnalyzerTask(ExpectedResults.RESOURCES_DIR);
        // Task interactions should be done from FX application thread
        interact((Callable<Object>)
                () -> {
                    try {
                        completionService.submit(task, new HashMap<>());
                        // limit waiting to 10 seconds - this should be more than enough for correct analysis
                        Future<Map<Path, List<NullCompareInst>>> res = completionService.poll(TIMEOUT_SECONDS, TimeUnit.SECONDS);
                        Assert.assertNotNull("AnalyzerTask should've finished by now", res);
                        Assert.assertNull("No new results should be in queue", completionService.poll());
                    } finally {
                        executor.shutdownNow();
                    }
                    return null;
                }
        );

        // Allow all events to complete then check for task state
        // This is needed because Tasks change of state is happening in FX application thread as another event (Platform.runLater)
        // Interact waits for JavaFX events to complete
        interact(() -> {
            Assert.assertTrue(task.getState() == Worker.State.SUCCEEDED);

            Map<Path, List<NullCompareInst>> results = task.analysisResultsProperty().get();
            ExpectedResults.REDUNDANT_PASSING.forEach(
                    (path, expectedResults) -> {
                        Assert.assertTrue(results.containsKey(path));
                        expectedResults.lineNumbers.sort(Integer::compareTo);
                        Assert.assertEquals(
                                results.get(path).stream()
                                        .mapToInt(value -> value.lineNumber())
                                        .sorted()
                                        .boxed()
                                        .collect(Collectors.<Integer>toList()),
                                expectedResults.lineNumbers);
                    });

            ExpectedResults.REDUNDANT_FAILING.forEach(
                    (path, expectedResults) -> {
                        Assert.assertTrue(results.containsKey(path));
                        expectedResults.lineNumbers.sort(Integer::compareTo);
                        Assert.assertNotEquals(
                                results.get(path).stream()
                                        .mapToInt(value -> value.lineNumber())
                                        .sorted()
                                        .boxed()
                                        .collect(Collectors.<Integer>toList()),
                                expectedResults.lineNumbers);
                    });
        });
    }
}
