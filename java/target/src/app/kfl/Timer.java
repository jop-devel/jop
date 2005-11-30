package kfl;

/**
*	A VERY simple timer and WD handling (!copy! for kfl).
*/

public class Timer {

	private static boolean blink;
	private static int next;
	private static int last;

	public static void init() {

		blink = true;
		wd();
	}

	public static void wd() {

		if (blink) {
			JopSys.wr(1, BBSys.IO_WD);
			blink = false;
		} else {
			JopSys.wr(0, BBSys.IO_WD);
			blink = true;
		}
	}

	public static void start() {

		next = JopSys.rd(JopSys.IO_CNT);
		last = next;
	}

	public static int usedTime() {

		return JopSys.rd(JopSys.IO_CNT)-last;
	}

	public static void waitForNextInterval() {

		next += JopSys.INTERVAL;

		int i = JopSys.rd(JopSys.IO_CNT);
		if (next-i < 0) {	// missed time!
			next = i;		// correct next
			last = i;
			return;
		}
		while (next-JopSys.rd(JopSys.IO_CNT) >= 0)
			;
		last = JopSys.rd(JopSys.IO_CNT);
	}

/**
*	next Interval in ms (with next increment)
*/
	static void waitForNextMs(int ms) {

		next += ms*JopSys.MS;

		int i = JopSys.rd(JopSys.IO_CNT);
		if (next-i < 0) {	// missed time!
			next = i;		// correct next
			last = i;
			return;
		}
		while (next-JopSys.rd(JopSys.IO_CNT) >= 0)
			;
		last = JopSys.rd(JopSys.IO_CNT);
	}

/**
*	simple wait val ms (no wd!, no next increment).
*/
	public static void sleep(int ms) {

		int j = JopSys.rd(JopSys.IO_CNT);
		for (int i=0; i<ms; ++i) {
			j += JopSys.MS;
			while (j-JopSys.rd(JopSys.IO_CNT) >= 0)
				;
		}
	}

/**
*	simple wait val ms with wd.
*/
	public static void sleepWd(int ms) {

		int j = JopSys.rd(JopSys.IO_CNT)+JopSys.MS;
		for (int i=0; i<ms; ++i) {
			while (j-JopSys.rd(JopSys.IO_CNT) >= 0)
				;
			j += JopSys.MS;
			wd();
		}
	}
}
