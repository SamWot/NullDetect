package test;

public class ExampleVirtualMethod extends test.ExampleBaseClass {
    @Override
    public String foo() {
        return "bar";
    }

    public void test1() {
        ExampleBaseClass base = new ExampleVirtualMethod();
        if (base.foo() != null) {
            System.out.println("foo() != null");
        }
    }
}
