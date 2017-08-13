package test;

public class ExamplePrivateMethod {
    private String foo() {
        return "foo";
    }

    public void test() {
        if (foo() == null) {
            System.out.println("foo() != null");
        }
    }
}