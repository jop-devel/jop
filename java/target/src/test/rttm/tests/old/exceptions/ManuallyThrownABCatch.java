package rttm.tests.old.exceptions;

/**
 * Works in HW.
 */
public class ManuallyThrownABCatch {

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		try {
			throw new ArrayIndexOutOfBoundsException();
		} catch (ArrayIndexOutOfBoundsException e) {
			System.out.println("Catched.");
		}
		
		System.out.println("Exiting.");
	}

}
