package javax.safetycritical.test;

import javax.safetycritical.MissionSequencer;
import joprt.RtThread;

import edu.purdue.scjtck.tck.TestSchedule406;

public class Minimal {

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		final TestSchedule406 test = new TestSchedule406();
		test.setup();
		MissionSequencer seq = test.getSequencer();
		new RtThread(5, 10000) {
			private TestSchedule406 tester = test;
			
			public void run() {
				while (!MissionSequencer.cleanupDidRun) {
					waitForNextPeriod();
				}
				tester.teardown();
			}
		};
		System.out.println("Hello SCJ World");
		seq.handleAsyncEvent(); // Starts the tests
	}

}