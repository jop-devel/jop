package rttm.tests.old.exceptions;

/**
 * Catched only in jsim simulation.
 */
public class ABUncatched {

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		int[] foo = new int[1];
		int baz = foo[17];
		
		System.out.println("Exiting.");
	}

}
