package org.sam.home;

import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.Type;
import org.objectweb.asm.tree.*;
import org.objectweb.asm.tree.analysis.AnalyzerException;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.sam.home.Utils.testResources;

public class NullInterpreterTest {
    private static Path resourceDir;
    private static Map<String, Utils.ExpectedExampleResults> examples;

    @BeforeClass
    public static void setUpClass() {
        resourceDir = Paths.get("src/test/resources", "class");
        examples = new HashMap<>();
        examples.put("Example1.class",
                new Utils.ExpectedExampleResults("Example1.java", Arrays.asList(5, 11, 17)));
        examples.put("Example2.class",
                new Utils.ExpectedExampleResults("Example2.java", Arrays.asList(5, 12)));
        examples.put("Example3.class",
                new Utils.ExpectedExampleResults("Example3.java", Arrays.asList(10)));
        examples.put("Example4.class",
                new Utils.ExpectedExampleResults("Example4.java", Arrays.asList(7)));
        examples.put("Example5.class",
                new Utils.ExpectedExampleResults("Example5.java", Arrays.asList(5)));
        // TODO: Example6, ArrayList?
        // TODO: Add automatic compilation of examples sources.
    }

    @Test
    public void Examples() throws IOException {
        testResources(
                resourceDir,
                examples,
                (fis) -> {
                    final ClassReader cr;
                    try {
                        cr = new ClassReader(fis);
                    } catch (IOException ex) {
                        throw new RuntimeException(ex);
                    }
                    final ClassNode cn = new ClassNode(Opcodes.ASM5);
                    cr.accept(cn, 0);
                    try {
                        return Main.filterRedundant(Main.findPotentialCompares(cn));
                    } catch (AnalyzerException ex) {
                        throw new RuntimeException(ex);
                    }
                });
    }

}
