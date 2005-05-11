package jbe;


/**
*	LowLevel time and output for PC
*/
public class LowLevel {

	static boolean init;
	static Perf p = Perf.getPerf();
	static long freq = p.highResFrequency();

	public static int timeMillis() {
    	return (int) System.currentTimeMillis();
	}

	public static int clockTicks() {
		long l = p.highResCounter();
		l = l*1000000/freq; // in us
		return (int) l;
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
