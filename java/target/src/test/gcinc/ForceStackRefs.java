package gcinc;

import com.jopdesign.sys.GC;
import joprt.RtThread;

public class ForceStackRefs {


	static class StackUser extends RtThread {

		static class Counter {
			public int val = 0;
			private int [] bloat;
			public Counter(int start) {
				val = start;
				bloat = new int[start&0x3ff];
			}
		}

		public StackUser(int prio, int us) {
			super(prio, us);
		}

		public void run() {
			int c = 0;
			for (;;) {
				Counter cnt = new Counter(c);
				c = cnt.val+1;
				waitForNextPeriod();
				cnt.val = c;
				waitForNextPeriod();
				if (c != cnt.val) {
					System.out.print("!");
				} else {
					System.out.print(".");
				}
				waitForNextPeriod();
			}
		}
	}

	public static void main(String [] args) {

 		new StackUser(13,  90000);
 		new GC.ScanThread(12, 50000);
		new StackUser(11, 100000);
		new StackUser(10,  101000);
		new StackUser(9,  102000);
		new StackUser(8,  103000);
		new StackUser(7,  104000);
		new StackUser(6,  105000);
		new StackUser(5,  106000);
		new StackUser(4,  107000);
		new StackUser(3,  108000);
 		new GC.GCThread(2,   660000);
		new StackUser(1,  661000);

 		GC.setConcurrent();

		RtThread.startMission();

		for(;;);

	}

}