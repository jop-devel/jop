package testrt;
import util.*;
import joprt.*;
import com.jopdesign.sys.Native;

//	Measure time with a full thread queue

public class ThreadFull {

	static class Busy extends Thread {

		private int w, c;

		Busy(int per, int ch) {
			w = per*90/100;
			c = ch;
		}

		public void run() {

			for (;;) {
				Dbg.wr(c);
				int ts = Native.rd(Native.IO_US_CNT);
				ts += w;
				// busy wait for period end
				while (ts-Native.rd(Native.IO_US_CNT)>0)
					;
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
		for (i=0; i<6; ++i) {
			new Busy(800000, i+'a').start();
		}

		RtThread.startMission();
		// do busy work
		//
		loop();
	}

	static void loop() {

		int ts = Native.rd(Native.IO_US_CNT);
		for (;;) {
			Dbg.wr('m');
			Timer.wd();
			ts += 1000000;
			while (ts-Native.rd(Native.IO_US_CNT)>0)
				;
		}
	}

}
