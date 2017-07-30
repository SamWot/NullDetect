/**
 * Created by Sam on 29.07.2017.
 */
public class Main {
    public int foo;

    public Main(int foo) {
        this.foo = foo;
    }

    public int Bar() {
        if (this.foo == -1) {
            return 100;
        } else {
            return 0;
        }
    }

    public static void main(String[] args) {
        System.out.println("bla-bla-bla");
    }
}
