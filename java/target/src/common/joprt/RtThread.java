/**
*	RtThread.java
*/

package joprt;

import com.jopdesign.sys.RtThreadImpl;

public class RtThread {

	RtThreadImpl thr;
	

	// not necessary
	// private RtThread() {};

	public RtThread(int prio, int us) {
	
		this(prio, us, 0);
	}

	public RtThread(int prio, int us, int off) {

		thr = new RtThreadImpl(this, prio, us, off);
	}


	public void run() {
		;							// nothing to do
	}


	public static void startMission() {
		
		RtThreadImpl.startMission();
	}


	public boolean waitForNextPeriod() {

		return thr.waitForNextPeriod();
	}


	/**
	*	dummy yield() for compatibility reason.
	*/
//	public static void yield() {}


	/**
	*	for 'soft' rt threads.
	*/

	public static void sleepMs(int millis) {
		
		RtThreadImpl.sleepMs(millis);
	}

	/**
	 * Waste CPU cycles to simulate work.
	 * @param us execution time in us
	 */
	public static void busyWait(int us) {
		
		RtThreadImpl.busyWait(us);
	}
	
}
