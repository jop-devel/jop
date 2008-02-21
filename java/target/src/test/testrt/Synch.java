package testrt;

import joprt.RtThread;
import util.*;

/**
 * Test synchronized methods
 * @author martin
 *
 */
public class Synch {

	static Synch syn = new Synch();
	/**
	 * @param args
	 */
	public static void main(String[] args) {

		new RtThread(2, 11000) {
			public void run() {

				for (;;) {
					syn.modify();
					waitForNextPeriod();
				}
			}
		};

		new RtThread(3, 5000) {
			public void run() {

				for (;;) {
					if (syn.read()!=0) {
						System.out.println("Synchronization error");
					}
					waitForNextPeriod();
				}
			}
		};

		new RtThread(1, 500000) {
			public void run() {

				for (;;) {
					System.out.print("*");
					Timer.wd();
					waitForNextPeriod();
				}
			}
		};
		
		RtThread.startMission();
	}

	int val;
	synchronized int read() {
		return val;
	}
	synchronized void modify() {
		val = 1;
		int t = Timer.getTimeoutMs(10);
		while (!Timer.timeout(t)) {
			; // busy wait
		}
		val = 0;
	}
}
