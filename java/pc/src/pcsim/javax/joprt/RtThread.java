/**
*	RtThread.java on PC for simulation of JOP
*/

package javax.joprt;

import com.jopdesign.sys.Native;

public class RtThread extends Thread {

	private boolean nextOk;
	private int period;			// period in cycles
private int next;

	private static boolean start;

	private RtThread() {};

	public RtThread(int ms) {
		// period = ms*Thread.MS;
period = ms*20000;
		nextOk = false;
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

}
