package test;
public class Example4 {
    public final Object foo() {
        return new Object();
    }
    public void test() {
        if (foo() != null) { // *
            System.out.println("foo() != null");
        }
    }
}
