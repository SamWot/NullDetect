package org.sam.home.ui;

import org.sam.home.analyzer.NullCompareInst;

import java.io.IOException;
import java.nio.file.*;
import java.util.concurrent.*;
import java.util.stream.Stream;

public class Main {

    public static void main(String[] args) {
        if (args.length == 0) {
            System.err.println("No directory specified");
            return;
        }

        final PathMatcher classMatcher = FileSystems.getDefault().getPathMatcher("glob:**.class");

        for (final String dirName: args) {
            final Path dir;
            try {
                dir = Paths.get(dirName);
            } catch (InvalidPathException ex) {
                System.err.println("Invalid directory name:" + dirName);
                continue;
            }

            final ExecutorService executor =
                    Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors() + 1);
            final CompletionService<AnalyzerJob.AnalyzerResult> completionService =
                    new ExecutorCompletionService<>(executor);

            try (final Stream<Path> dirStream = Files.walk(dir)) {
                long filesNumber = dirStream
                        .filter(path -> classMatcher.matches(path))
                        .filter(path -> Files.isRegularFile(path, LinkOption.NOFOLLOW_LINKS))
                        .peek(path -> completionService.submit(new AnalyzerJob(path)))
                        .count();

                for (int i = 0; i < filesNumber; i++) {
                    Future<AnalyzerJob.AnalyzerResult> future = completionService.take();
                    try {
                        AnalyzerJob.AnalyzerResult result = future.get();
                        System.out.println("In class " + result.getFile().getFileName() + " found "
                                + result.getRedundantInsts().size() + " redundant null checks:");
                        for (final NullCompareInst inst: result.getRedundantInsts()) {
                            System.out.println(inst.lineInfo());
                        }
                        System.out.println();
                    } catch (ExecutionException ex) {
                        System.err.println("Something went wrong during analyze");
                        ex.printStackTrace();
                        continue;
                    }
                }
            } catch (IOException ex) {
                System.err.println("Can't find directory:" + dirName);
                continue;
            } catch (SecurityException ex) {
                System.err.println("Can't access directory:" + dirName);
                continue;
            } catch (InterruptedException ex) {
                System.err.println("Interrupted");
            } finally {
                executor.shutdownNow();
            }

            System.out.println();
        }
    }
}
