package test;

public final class ExampleVirtualMethod2 extends test.ExampleBaseClass {
    @Override
    public String foo() {
        return "bar";
    }

    public void test1() {
        ExampleBaseClass base = new ExampleVirtualMethod2();
        if (base.foo() != null) {
            System.out.println("foo() != null");
        }
    }
}
