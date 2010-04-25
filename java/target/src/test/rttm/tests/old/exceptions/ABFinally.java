package rttm.tests.old.exceptions;

/**
 * Catched only in jsim simulation.
 */
public class ABFinally {

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		try {
			int[] foo = new int[1];
			int baz = foo[17];
		} finally {
			System.out.println("Finally.");
		}
		
		System.out.println("Exiting.");
	}

}
