/**
*	RtThread.java on PC for simulation of JOP
*/

package joprt;

import com.jopdesign.sys.Native;

public class RtThread {

	private boolean nextOk;
	private int period;			// period in us
	private int offset;			// offset in us
private int next;

	private static boolean start;

	public RtThread(int prio, int us) {
	
		this(prio, us, 0);
	}

	public RtThread(int prio, int us, int off) {

		period = us;
		nextOk = false;

		new Thread() {
			public void run() {

				runner();
			}
		}.start();
	}

	public void run() {
	}

	void runner() {

		waitForMission();
		run();
	}

	public static void startMission() {
		start = true;
	}

	public void waitForMission() {

		while (!start) {
			yield();
		}
		nextOk = true;
		next = Native.rd(Native.IO_CNT);
	}


	public boolean waitForNextPeriod() {

		if (!nextOk) {
			next = Native.rd(Native.IO_CNT);		// this should not happen!
			nextOk = true;							// you forgot to wait on start mission
		}

		next += period;

		int i = Native.rd(Native.IO_CNT);
		if (next-i < 0) {							// missed time!
			next = i;								// correct next
			return false;
		}
/*
		state = WAITING;
		yield();
*/
		while (next-Native.rd(Native.IO_CNT) >= 0) {	// 'busy' wait with yield.
try { Thread.sleep(1); } catch (Exception e) {}
			yield();
		}
		return true;
	}

	/**
	*	dummy yield() for compatibility reason.
	*/
	public static void yield() {}


	public static void sleepMs(int millis) {
	
		int next = Native.rd(Native.IO_US_CNT)+millis*1000;
		while (Native.rd(Native.IO_US_CNT)-next < 0) {
try { Thread.sleep(1); } catch (Exception e) {}
			yield();
		}
	}
}
