package javax.safetycritical.test;

import javax.safetycritical.Mission;
import javax.safetycritical.MissionSequencer;
import javax.safetycritical.Safelet;
import javax.safetycritical.annotate.Level;

import joprt.RtThread;

import com.jopdesign.sys.RtThreadImpl;

import edu.purdue.scjtck.tck.TestSchedule402;

public class Minimal {

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		final TestSchedule402 test = new TestSchedule402();
		test.setup();
		MissionSequencer seq = test.getSequencer();
		new RtThread(5, 10000) {
			private TestSchedule402 tester = test;
			
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