package org.sam.home;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

public final class ExpectedResults {
    private ExpectedResults() {}

    public static final Path RESOURCES_DIR;

    public static final Map<Path, Utils.ExpectedExampleResults> POTENTIAL;

    public static final Map<Path, Utils.ExpectedExampleResults> REDUNDANT_PASSING;
    public static final Map<Path, Utils.ExpectedExampleResults> REDUNDANT_FAILING;

    static {
        // TODO: Example6?
        // TODO: Add automatic compilation of examples sources.

        RESOURCES_DIR = Paths.get("src/test/resources", "class");

        POTENTIAL = new HashMap<>();
        REDUNDANT_PASSING = new HashMap<>();
        REDUNDANT_FAILING = new HashMap<>();

        // Simple examples
        POTENTIAL.put(resourcePath("Example1.class"),
                new Utils.ExpectedExampleResults("Example1.java", Arrays.asList(5, 11, 17)));
        POTENTIAL.put(resourcePath("Example2.class"),
                new Utils.ExpectedExampleResults("Example2.java", Arrays.asList(4, 5, 12)));
        POTENTIAL.put(resourcePath("Example3.class"),
                new Utils.ExpectedExampleResults("Example3.java", Arrays.asList(10)));
        POTENTIAL.put(resourcePath("Example4.class"),
                new Utils.ExpectedExampleResults("Example4.java", Arrays.asList(7)));
        POTENTIAL.put(resourcePath("Example5.class"),
                new Utils.ExpectedExampleResults("Example5.java", Arrays.asList(5)));
        POTENTIAL.put(resourcePath("ExampleStaticMethod.class"),
                new Utils.ExpectedExampleResults("ExampleStaticMethod.java", Arrays.asList(17, 23)));
        POTENTIAL.put(resourcePath("ExampleVirtualMethod.class"),
                new Utils.ExpectedExampleResults("ExampleVirtualMethod.java", Arrays.asList(16, 23)));
        POTENTIAL.put(resourcePath("ExampleVirtualMethod2.class"),
                new Utils.ExpectedExampleResults("ExampleVirtualMethod2.java", Arrays.asList(11)));
        POTENTIAL.put(resourcePath("ExamplePrivateMethod.class"),
                new Utils.ExpectedExampleResults("ExamplePrivateMethod.java", Arrays.asList(9)));

        // ArrayList
        POTENTIAL.put(resourcePath("ArrayList", "ArrayList.class"),
                new Utils.ExpectedExampleResults("ArrayList.java", Arrays.asList(311, 313, 331, 333, 520, 522)));
        POTENTIAL.put(resourcePath("ArrayList", "ArrayList$1.class"),
                new Utils.ExpectedExampleResults("ArrayList.java", Arrays.asList()));
        POTENTIAL.put(resourcePath("ArrayList", "ArrayList$ArrayListSpliterator.class"),
                new Utils.ExpectedExampleResults("ArrayList.java", Arrays.asList(1327, 1345, 1362, 1364, 1364)));
        POTENTIAL.put(resourcePath("ArrayList", "ArrayList$Itr.class"),
                new Utils.ExpectedExampleResults("ArrayList.java", Arrays.asList()));
        POTENTIAL.put(resourcePath("ArrayList", "ArrayList$ListItr.class"),
                new Utils.ExpectedExampleResults("ArrayList.java", Arrays.asList()));
        POTENTIAL.put(resourcePath("ArrayList", "ArrayList$SubList.class"),
                new Utils.ExpectedExampleResults("ArrayList.java", Arrays.asList()));
        POTENTIAL.put(resourcePath("ArrayList", "ArrayList$SubList$1.class"),
                new Utils.ExpectedExampleResults("ArrayList.java", Arrays.asList()));

        // Simple examples
        REDUNDANT_PASSING.put(resourcePath("Example1.class"),
                new Utils.ExpectedExampleResults("Example1.java", Arrays.asList(5, 11, 17)));
        REDUNDANT_PASSING.put(resourcePath("Example4.class"),
                new Utils.ExpectedExampleResults("Example4.java", Arrays.asList(7)));
        REDUNDANT_PASSING.put(resourcePath("Example5.class"),
                new Utils.ExpectedExampleResults("Example5.java", Arrays.asList(5)));
        REDUNDANT_PASSING.put(resourcePath("ExampleStaticMethod.class"),
                new Utils.ExpectedExampleResults("ExampleStaticMethod.java", Arrays.asList(17)));
        REDUNDANT_PASSING.put(resourcePath("ExampleVirtualMethod.class"),
                new Utils.ExpectedExampleResults("ExampleVirtualMethod.java", Arrays.asList(23)));
        REDUNDANT_PASSING.put(resourcePath("ExampleVirtualMethod2.class"),
                new Utils.ExpectedExampleResults("ExampleVirtualMethod2.java", Arrays.asList(11)));
        REDUNDANT_PASSING.put(resourcePath("ExamplePrivateMethod.class"),
                new Utils.ExpectedExampleResults("ExamplePrivateMethod.java", Arrays.asList(9)));
        // currently failing:
        REDUNDANT_FAILING.put(resourcePath("Example2.class"),
                new Utils.ExpectedExampleResults("Example2.java", Arrays.asList(5, 12)));
        REDUNDANT_FAILING.put(resourcePath("Example3.class"),
                new Utils.ExpectedExampleResults("Example3.java", Arrays.asList(10)));

        // ArrayList
        REDUNDANT_PASSING.put(resourcePath("ArrayList", "ArrayList.class"),
                new Utils.ExpectedExampleResults("ArrayList.java", Arrays.asList()));
        REDUNDANT_PASSING.put(resourcePath("ArrayList", "ArrayList$1.class"),
                new Utils.ExpectedExampleResults("ArrayList.java", Arrays.asList()));
        REDUNDANT_PASSING.put(resourcePath("ArrayList", "ArrayList$ArrayListSpliterator.class"),
                new Utils.ExpectedExampleResults("ArrayList.java", Arrays.asList()));
        REDUNDANT_PASSING.put(resourcePath("ArrayList", "ArrayList$Itr.class"),
                new Utils.ExpectedExampleResults("ArrayList.java", Arrays.asList()));
        REDUNDANT_PASSING.put(resourcePath("ArrayList", "ArrayList$ListItr.class"),
                new Utils.ExpectedExampleResults("ArrayList.java", Arrays.asList()));
        REDUNDANT_PASSING.put(resourcePath("ArrayList", "ArrayList$SubList.class"),
                new Utils.ExpectedExampleResults("ArrayList.java", Arrays.asList()));
        REDUNDANT_PASSING.put(resourcePath("ArrayList", "ArrayList$SubList$1.class"),
                new Utils.ExpectedExampleResults("ArrayList.java", Arrays.asList()));
    }


    static Path resourcePath(String ...parts) {
        return Paths.get(RESOURCES_DIR.toString(), parts);
    }
}
