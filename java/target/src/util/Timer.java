package util;

/**
*	A VERY simple timer and WD handling.
*
*	A little excures in modulo clac:
*
*		get the difference of to values (on wrap over):
*
*	thats ok:		val-Native.rd(Native.IO_CNT) < 0;
*
*	thats WRONG:	val < Native.rd(Native.IO_CNT);
*/

import com.jopdesign.sys.*;

public class Timer {

	private static boolean blink;

	public static int cnt() {

		return Native.rd(Const.IO_CNT);
	}

	public static int us() {

		return Native.rd(Const.IO_US_CNT);
	}

	public static boolean timeout(int val) {

		return val-Native.rd(Const.IO_US_CNT) < 0;
	}
	public static int getTimeoutMs(int msOff) {

		return Native.rd(Const.IO_US_CNT) + 1000*msOff;
	}
	public static int getTimeoutSec(int sec) {

		return Native.rd(Const.IO_US_CNT) + 1000*1000*sec;
	}

	public static void wd() {

		if (blink) {
			Native.wr(1, Const.IO_WD);
			blink = false;
		} else {
			Native.wr(0, Const.IO_WD);
			blink = true;
		}
	}

/**
*	simple wait val t (no wd!, no next increment).
*/
	// for Amd.progam()
	public static void usleep(int t) {

		int j = Native.rd(Const.IO_US_CNT);
		j += t;
		while (j-Native.rd(Const.IO_US_CNT) >= 0)
			;
	}

	//
	//	A very simple clock.
	//	Call loop() sometimes to update the clock;
	//
	private static boolean started;
	private static int day, second, s;
	private static int next;
	
	/**
	 * Call this more than once a second to get the second
	 * timer function.
	 */
	public static void loop() {
		
		if (!started) init();
		if (next-Native.rd(Const.IO_US_CNT) < 0) {
			++s; ++second;
			if (second==86400) {
				second = 0;
				++day;
			}
			next += 1000000;
		}
	}
	
	public static int getSec() {
		return s;
	}
	
	/**
	 * A timeout with integer as second counter is
	 * good for intervals shorter than 68 years.
	 * @param val
	 * @return
	 */
	public static boolean secTimeout(int val) {

		return val-s < 0;
	}
	
	private static void init() {
		
		day = second = s = 0;
		next = Native.rd(Const.IO_US_CNT)+1000000;
		started = true;
	}
}
