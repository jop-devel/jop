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

		char id;

		public StackUser(int prio, int us, char id) {
			super(prio, us);
			this.id = id;
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
					System.out.print('!');
				} else {
					System.out.print(id);
				}
				waitForNextPeriod();
			}
		}
	}

	public static void main(String [] args) {

 		new StackUser(14,  90000, 'A');
		new StackUser(13, 100000, 'B');
		new StackUser(12, 101000, 'C');
 		new GC.ScanEvent(11, 330000);
		new StackUser(10,  102000, 'D');
		new StackUser(9,  103000, 'E');
		new StackUser(8,  104000, 'F');
		new StackUser(7,  105000, 'G');
		new StackUser(6,  106000, 'H');
		new StackUser(5,  107000, 'I');
 		new GC.GCThread(4, 330000);
		new StackUser(3,  350000, 'K');
		new StackUser(2,  360000, 'L');
		new StackUser(1,  370000, 'M');

 		GC.setConcurrent();

		RtThread.startMission();

		for(;;);

	}

}