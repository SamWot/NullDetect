package test;
public class Example5 {
    private static final String MESSAGE = "Hello, world!";
    public void test() {
        if (MESSAGE != null) { // *
            System.out.println(MESSAGE);
        }
    }
}