package jbe;


/**
*	LowLevel time and output for aJile
*/
public class LowLevel {

	static boolean init;

	public static int timeMillis() {
    	return (int) System.currentTimeMillis();
	}

	public static int clockTicks() {
		return (int) com.ajile.jem.rawJEM.getTime();
	}

	public static void msg(String msg) {

		System.out.print(msg);
		System.out.print(" ");
	}

	public static void msg(int val) {

		System.out.print(val);
		System.out.print(" ");
	}

	public static void msg(String msg, int val) {

// System.out.println("freq: "+freq);
		msg(msg);
		msg(val);
	}

	public static void lf() {

		System.out.println();
	}
}
