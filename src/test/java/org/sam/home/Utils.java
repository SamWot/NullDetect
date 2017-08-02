package org.sam.home;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.PathMatcher;
import java.util.stream.Stream;

public final class Utils {
    private Utils() {}

    static Path findResourceFile(final Path resourceDir, final String fileName) throws IOException {
        final PathMatcher nameFilter = resourceDir.getFileSystem().
                getPathMatcher("glob:**" + fileName);
        try (final Stream<Path> stream = Files.list(resourceDir)) {
            return stream.filter(nameFilter::matches).findFirst().get();
        }
    }
}
