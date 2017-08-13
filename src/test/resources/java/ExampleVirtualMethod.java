package test;

public class ExampleVirtualMethod extends test.ExampleBaseClass {
    @Override
    public String foo() {
        return "bar";
    }

    @Override
    public final String bar() {
        return "buz";
    }

    public void test1() {
        ExampleBaseClass base = new ExampleVirtualMethod();
        if (base.foo() != null) {
            System.out.println("foo() != null");
        }
    }

    public void test2() {
        ExampleBaseClass base = new ExampleVirtualMethod();
        if (base.bar() != null) {
            System.out.println("bar() != null");
        }
    }
}
