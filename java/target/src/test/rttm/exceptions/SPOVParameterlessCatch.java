package rttm.exceptions;

public class SPOVParameterlessCatch {

	private static void recursive()
	{
		recursive();
	}
	
	/**
	 * @param args
	 */
	public static void main(String[] args) {
		try {
			recursive();
		} catch (StackOverflowError e) {
			System.out.println("Catched.");
		}
		
		System.out.println("Finished.");
	}

}
