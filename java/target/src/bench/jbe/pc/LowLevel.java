package jbe;

/**
*	LowLevel time and output for PC
*/
public class LowLevel {

	static boolean init;

	static int timeMillis() {
    	return (int) System.currentTimeMillis();
	}

	static void msg(String msg) {

		System.out.print(msg);
		System.out.print(" ");
	}

	static void msg(int val) {

		System.out.print(val);
		System.out.print(" ");
	}

	static void msg(String msg, int val) {

		msg(msg);
		msg(val);
	}

	static void lf() {

		System.out.println();
	}
}
