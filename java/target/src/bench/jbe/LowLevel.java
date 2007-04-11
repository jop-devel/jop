package jbe;

// JOP system functions for higher accurate timers
// import com.jopdesign.sys.Const;
// import com.jopdesign.sys.Native;

/**
 * LowLevel time and output for the benchmark.
 * 
 * Special versions for aJile, lejos,... are in comments.
 */
public class LowLevel {

	/**
	 * Clock frequency for the target to calculate clock cycles
	 * of the micro benchmarks. Set to 0 if not known.
	 * Works only up to 2 GHz (integer overflow). However, that
	 * should be ok for embedded devices ;-)
	 */
	public static final int FREQ = 60;

	static boolean init;

	// Performance counter for the PC JVM
	// static Perf p = Perf.getPerf();
	// static long freq = p.highResFrequency();

	/**
	 * Get the current time in milli seconds (can be relative) as an integer.
	 * 
	 */
	public static int timeMillis() {

		// JOP version with the us timer
		// return Native.rd(Const.IO_US_CNT)/1000;
		
		return (int) System.currentTimeMillis();
	}

	/**
	 * Get the current time in micro seconds.
	 * 
	 * @return
	 */
	public static int timeMicros() {

		// PC version
		// long l = p.highResCounter();
		// l = l*1000000/freq; // in us
		// return (int) l;
		
		// JOP version
		// return Native.rd(Const.IO_US_CNT);


		// aJile version
		// return (int) com.ajile.jem.rawJEM.getTime();

		// clock ticks for gcj (Linux)
		// return (int) (jbe.gcj.TSC.read()/267); // 267 is 266.6 MHz clock
		
		return 0;
	}

	/**
	 * Print a String message and append a ' '
	 * @param msg output message
	 */
	public static void msg(String msg) {

		// lejos version
		// TextLCD.print(msg);
		// try { Thread.sleep(1500); } catch (Exception e) {}
		
		System.out.print(msg);
		System.out.print(" ");
	}

	/**
	 * Print an integer.
	 * @param val output value
	 */
	public static void msg(int val) {

		// lejos version
		// LCD.showNumber(val/1000);
		// try { Thread.sleep(1500); } catch (Exception e) {}
		// LCD.showNumber(val%1000);
		// try { Thread.sleep(1500); } catch (Exception e) {}

		System.out.print(val);
		System.out.print(" ");
	}

	/**
	 * Print a message and an integer
	 * @param msg String message
	 * @param val integer value
	 */
	public static void msg(String msg, int val) {

		msg(msg);
		msg(val);
	}

	/**
	 * Start a new line.
	 *
	 */
	public static void lf() {

		// no lf on lejos version
		//
		
		System.out.println();
	}
}
