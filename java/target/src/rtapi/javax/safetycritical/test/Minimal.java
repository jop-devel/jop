package javax.safetycritical.test;

import javax.safetycritical.JopSystem;
import javax.safetycritical.MissionSequencer;

import joprt.RtThread;


public class Minimal {

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		/*final ClockAccuracy test = new ClockAccuracy();
		test.setup();
		MissionSequencer seq = test.getSequencer();
		new RtThread(5, 10000) {
			private ClockAccuracy tester = test;
			
			public void run() {
				while (!MissionSequencer.cleanupDidRun) {
					waitForNextPeriod();
				}
				tester.teardown();
			}
		};
		System.out.println("Hello SCJ World");
		seq.handleAsyncEvent(); // Starts the tests*/
		JopSystem.startMission(new ScopeTest());
	}

}