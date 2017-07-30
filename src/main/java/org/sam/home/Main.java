package org.sam.home;

import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.Opcodes;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;

/**
 * Main class
 */
public class Main {
    public static void main(String[] args) {
        if (args.length == 0) {
            System.err.println("No file name specified");
            return;
        }

        for (String fileName: args) {
            try (final FileInputStream fis = new FileInputStream(fileName)) {
                detect(fis, fileName);
            } catch (FileNotFoundException ex) {
                System.err.println("No such file: " + fileName);
            } catch (IOException ex) {
                System.err.println("Something gone wrong during file close: " + fileName);
                ex.printStackTrace();
            }
        }
    }

    public static void detect(final FileInputStream fis, final String fileName) {
        final ClassReader cr;
        try {
             cr = new ClassReader(fis);
        } catch (IOException ex) {
            System.err.println("Can't read file: " + fileName);
            return;
        }

        cr.accept(new ClassVisitor(Opcodes.ASM5) {
            @Override
            public void visitSource(String source, String debug) {
                System.out.println("foo bar");
                System.out.println(source);
                System.out.println(debug);
                super.visitSource(source, debug);
            }
        },0);

    }
}
