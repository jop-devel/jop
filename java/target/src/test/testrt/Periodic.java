package testrt;
import util.*;
import joprt.*;
import com.jopdesign.sys.Native;

public class Periodic {

	public static void main(String[] args) {

		Dbg.initSer();				// use serial line for debug output

		RtThread rt = new RtThread(10, 100000) {
			public void run() {

				int ts, ts_old;

				waitForNextPeriod();
				ts_old = Native.rd(Native.IO_US_CNT);

				for (;;) {
					waitForNextPeriod();
					ts = Native.rd(Native.IO_US_CNT);
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
Dbg.wr('*');
					waitForNextPeriod();
					int ts = Native.rd(Native.IO_US_CNT) + 990000;
					while (ts-Native.rd(Native.IO_US_CNT)>0)
						;
				}
			}
		};

		RtThread.startMission();

		// sleep
		for (;;) {
Dbg.wr('M');
// RtThread.debug();
			Timer.wd();
			try { RtThread.sleepMs(1200); } catch (Exception e) {}
		}
	}

}
