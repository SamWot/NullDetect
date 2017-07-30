package test;
public class Example3 {
    private final Object x;
    
    public Example3() {
        x = new Object();
    }
    
    public void test() {
        if (x == null) { // *
            System.out.println("x == null");
        }
    }
}
