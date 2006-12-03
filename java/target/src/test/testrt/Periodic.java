package testrt;
import joprt.RtThread;
import util.Dbg;
import util.Timer;

import com.jopdesign.sys.Const;
import com.jopdesign.sys.Native;

public class Periodic {

	public static void main(String[] args) {

		Dbg.initSerWait();				// use serial line for debug output

		RtThread rt = new RtThread(10, 100000) {
			public void run() {

				int ts, ts_old;

				waitForNextPeriod();
				ts_old = Native.rd(Const.IO_US_CNT);

				for (;;) {
					waitForNextPeriod();
					ts = Native.rd(Const.IO_US_CNT);
					Result.printPeriod(ts_old, ts);
					ts_old = ts;
				}
			}
		};

		//
		// do busy work
		//

		RtThread rts = new RtThread(9, 1000000) {
			public void run() {
				for (;;) {
					System.out.print('*');
					waitForNextPeriod();
					int ts = Native.rd(Const.IO_US_CNT) + 990000;
					while (ts-Native.rd(Const.IO_US_CNT)>0)
						;
				}
			}
		};

		RtThread.startMission();

		// sleep
		for (;;) {
			System.out.print('m');
// RtThread.debug();
			Timer.wd();
			RtThread.sleepMs(1200);
		}
	}

}
