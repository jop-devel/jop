package joptimizer.tests.stack;

/**
 * test() method has return with non-empty stack
 * TODO generate (StackTest.class in CVS is modified by hand with JavaBytecodeEditor!)
 */
public class StackTest {

    public static int test(int i, String text) {
        if ( text == null ) {
            return 0;
        }
        return i;
    }

    public static void main(String[] args) {
        int j = test(1, null);

        if ( j == 1 ) {
            System.out.println("Test failed.");
        } else {
            System.out.println("Test OK.");
        }
    }
}
