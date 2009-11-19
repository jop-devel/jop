package rttm.exceptions;

public class SPOVParameterless {

	private static void recursive()
	{
		recursive();
	}
	
	/**
	 * @param args
	 */
	public static void main(String[] args) {
		recursive();
	}

}
