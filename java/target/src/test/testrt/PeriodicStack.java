package testrt;
import util.*;
import joprt.*;
import com.jopdesign.sys.Native;

//
//	increase stack in busy thread (1+5 per method call)
//

public class PeriodicStack {

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

		//
		// do busy work
		//

		RtThread rts = new RtThread(9, 1000000) {
			public void run() {
				f1();
			}
			void f1() { f2(); }
			void f2() { f3(); }
			void f3() { f4(); }
			void f4() { f5(); }
			void f5() { f6(); }
			void f6() { f7(); }
			void f7() { f8(); }
			void f8() { f9(); }
			void f9() { f10(); }
			void f10() { loop(); }

			void loop() {
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
			Timer.wd();
			try { Thread.sleep(1200); } catch (Exception e) {}
		}
	}
}
