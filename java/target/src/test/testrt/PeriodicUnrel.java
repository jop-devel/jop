package testrt;
import util.*;
import joprt.*;
import com.jopdesign.sys.Native;

//	use differnet (unrelated) period to find WC Jitter

public class PeriodicUnrel {

	static class Busy extends RtThread {

		private int c;

		Busy(int per, int ch) {
			super(5, per);
			c = ch;
		}

		public void run() {
			for (;;) {
//				Dbg.wr(c);
				waitForNextPeriod();
			}
		}
	}
	
	public static void main(String[] args) {

		Dbg.initSer();				// use serial line for debug output

		RtThread rt = new RtThread(10, 100000) {
			public void run() {

				waitForNextPeriod();
				int ts_old = Native.rd(Native.IO_US_CNT);

				for (;;) {
					waitForNextPeriod();
					int ts = Native.rd(Native.IO_US_CNT);
					Result.printPeriod(ts_old, ts);
					ts_old = ts;
				}
			}
		};

		int i;
		for (i=0; i<10; ++i) {
			new Busy(2345+456*i, i+'a');
		}

		RtThread.startMission();

RtThread.debug();
		// sleep
		for (;;) {
// RtThread.debug();
Dbg.wr('M');
			Timer.wd();
for (;;) ;
			// try { Thread.sleep(1200); } catch (Exception e) {}
		}
	}
}
