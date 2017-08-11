package test;

import java.util.Arrays;

public class ExampleStaticMethod {
    public static Object foo() {
        return new Object();
    }

    public static Object foo(int i) {
        return null;
    }

    public static String bar() { return null; }

    public void test1() {
        if (foo() != null) { // *
            System.out.println("foo() != null");
        }
    }

    public void test2() {
        if ( Arrays.copyOf(new int[] {1, 2, 3}, 3) != null) {
            System.out.println("foo() != null");
        }
    }
}
