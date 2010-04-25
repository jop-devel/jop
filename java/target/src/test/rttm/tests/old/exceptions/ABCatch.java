package rttm.tests.old.exceptions;

/**
 * Catched only in jsim simulation.
 */
public class ABCatch {

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		try {
			int[] foo = new int[1];
			int baz = foo[17];
		} catch (ArrayIndexOutOfBoundsException e) {
			System.out.println("Catched.");
		}
		
		System.out.println("Exiting.");
	}

}
