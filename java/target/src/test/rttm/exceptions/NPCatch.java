package rttm.exceptions;

/**
 * Catched.
 */
public class NPCatch {

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		Object none = null;
		
		try {
			none.hashCode();
		} catch (NullPointerException e) {
			System.out.println("Catched.");
		}
		
		System.out.println("Finishing.");
	}

}
