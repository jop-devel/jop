package testrt;
import util.*;
import joprt.*;
import com.jopdesign.sys.*;

//	Measure time with a full thread queue

public class PeriodicFull {

	static class Busy extends RtThread {

		private int w, c;

		Busy(int per, int ch) {
			super(5, per);
			w = per*90/100;
w = per*12/100;
			c = ch;
		}

		public void run() {
			for (;;) {
				Dbg.wr(c);
				int ts = Native.rd(Const.IO_US_CNT);
				ts += w;
				// busy wait for period end
				while (ts-Native.rd(Const.IO_US_CNT)>0)
					;
				waitForNextPeriod();
			}
		}
	}
	
	public static void main(String[] args) {

		Dbg.initSer();				// use serial line for debug output

		RtThread rt = new RtThread(10, 100000) {
			public void run() {

				waitForNextPeriod();
				int ts_old = Native.rd(Const.IO_US_CNT);

				for (;;) {
					waitForNextPeriod();
					int ts = Native.rd(Const.IO_US_CNT);
//					Result.printPeriod(ts_old, ts);
					ts_old = ts;
				}
			}
		};

		int i;
		for (i=0; i<6; ++i) {
			new Busy(1000000, i+'a');
		}

		RtThread.startMission();

		// sleep
		for (;;) {
// RtThread.debug();
			Timer.wd();
			RtThread.sleepMs(1200);
			Dbg.wr('M');
		}
	}
}
