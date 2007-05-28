package gcinc;

import joprt.RtThread;

import com.jopdesign.sys.GC;

public class Concurrent {
	

	static SimpVector a, b, c;
	/**
	 * @param args
	 */
	public static void main(String[] args) {
		
		a = new SimpVector(100);
		b = new SimpVector(25);
		c = new SimpVector(999);

		new RtThread(2, 10000) {
			public void run() {
				for (;;) {
					a.run();
					waitForNextPeriod();
				}
			}			
		};

		new RtThread(1, 3456) {
			public void run() {
				for (;;) {
//					int time = RtSystem.currentTimeMicro();
					System.out.print("G");
					GC.gc();
//					int now = RtSystem.currentTimeMicro();
//					System.out.println(now-time);
//					time = now;
					
//					waitForNextPeriod();
				}
			}
		};
		
		GC.setConcurrent();
		RtThread.startMission();
	}

}
