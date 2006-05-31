package wcet.kflapp;

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

	static int usedTime() {

		return JopSys.rd(JopSys.IO_CNT)-last;
	}

	static void waitForNextInterval() {

		next += JopSys.INTERVAL;

		int i = JopSys.rd(JopSys.IO_CNT);
		if (next-i < 0) {	// missed time!
			next = i;		// correct next
			last = i;
			return;
		}
		while (next-JopSys.rd(JopSys.IO_CNT) >= 0) // @WCA loop=1
			;
		last = JopSys.rd(JopSys.IO_CNT);
	}


}
