package test;
public class Example2 {
    public static void test1(Object x) {
        if (x == null) return;
        if (x != null) { // *
            System.out.println("x != null");
        }
    }
    
    public static void test2(Object x) {
        System.out.println(x.hashCode());
        if (x == null) { // *
            System.out.println("x == null");
        }
    }
}