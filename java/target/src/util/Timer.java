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
	private static int next;
	private static int last;

	private static int interval;

	public static void init(int f, int iv) {

		interval = 1000*iv;
		blink = true;
		wd();				// make WD happy
		wd();
		wd();
	}

	public static int cnt() {

		return Native.rd(Native.IO_CNT);
	}

	public static int us() {

		return Native.rd(Native.IO_US_CNT);
	}


	public static int getNextCnt(int msOff) {

		return Native.rd(Native.IO_US_CNT) + 1000*msOff;
	}

	public static int getNextCnt(int lastVal, int msOff) {

		return lastVal + 1000*msOff;
	}

	public static boolean timeout(int val) {

		return val-Native.rd(Native.IO_US_CNT) < 0;
	}
	public static int getTimeoutMs(int msOff) {

		return Native.rd(Native.IO_US_CNT) + 1000*msOff;
	}
	public static int getTimeoutSec(int sec) {

		return Native.rd(Native.IO_US_CNT) + 1000*1000*sec;
	}

	public static void wd() {

		if (blink) {
			Native.wr(1, Native.IO_WD);
			blink = false;
		} else {
			Native.wr(0, Native.IO_WD);
			blink = true;
		}
	}

	public static void start() {

		next = Native.rd(Native.IO_US_CNT);
		last = next;
	}

	public static int usedTime() {

		return Native.rd(Native.IO_US_CNT)-last;
	}

	public static void waitForNextInterval() {

		next += interval;

		int i = Native.rd(Native.IO_US_CNT);
		if (next-i < 0) {	// missed time!
			next = i;		// correct next
			last = i;
			return;
		}
		while (next-Native.rd(Native.IO_US_CNT) >= 0)
			;
		last = Native.rd(Native.IO_US_CNT);
	}

/**
*	next Interval in ms (with next increment)
*/
	public static void waitForNextMs(int t) {

		next += t*1000;

		int i = Native.rd(Native.IO_US_CNT);
		if (next-i < 0) {	// missed time!
			next = i;		// correct next
			last = i;
			return;
		}
		while (next-Native.rd(Native.IO_US_CNT) >= 0)
			;
		last = Native.rd(Native.IO_US_CNT);
	}

/**
*	simple wait val t (no wd!, no next increment).
*/
	public static void sleep(int t) {

		int j = Native.rd(Native.IO_US_CNT);
		for (int i=0; i<t; ++i) {
			j += 1000;
			while (j-Native.rd(Native.IO_US_CNT) >= 0)
				;
		}
	}

/**
*	simple wait val t (no wd!, no next increment).
*/
	// for Amd.progam()
	public static void usleep(int t) {

		int j = Native.rd(Native.IO_US_CNT);
		j += t;
		while (j-Native.rd(Native.IO_US_CNT) >= 0)
			;
	}

/**
*	simple wait val t with wd.
*/
	public static void sleepWd(int t) {

		int j = Native.rd(Native.IO_US_CNT)+1000;
		for (int i=0; i<t; ++i) {
			while (j-Native.rd(Native.IO_US_CNT) >= 0)
				;
			j += 1000;
			wd();
		}
	}
}
