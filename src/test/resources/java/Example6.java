package test;
public class Example6 {
    public void test() {
        final Object nullConst = null;
        final String str = "foo bar";
        if (str != nullConst) { // *
            System.out.println(str);
        }
    }
}