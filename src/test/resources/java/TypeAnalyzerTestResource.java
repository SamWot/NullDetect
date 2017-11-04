package test;

public class TypeAnalyzerTest {
    public void ldcString() {
        String foo = "foo";
    }

    public void ldcObject() {
        Object foo = "foo";
    }

    public void newString() {
        Object foo = new String();
    }

    public void checkcast() {
        Object foo = "foo";
        String bar = (String) foo;
    }

    public void invoke() {
        Object foo = String.join("foo", "bar");
    }
}