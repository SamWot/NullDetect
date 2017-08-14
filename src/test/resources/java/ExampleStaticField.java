package test;

public class ExampleStaticField {
    public static final String foo = String.join("foo", "bar");

    public void test() {
        if (foo == null) {
            System.out.println("foo = null");
        }
    }
}