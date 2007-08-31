package testrt;
import joprt.RtThread;
import util.Dbg;
import util.Timer;

import com.jopdesign.sys.Const;
import com.jopdesign.sys.Native;

//
//	increase stack in busy thread (1+5 per method call)
//

public class PeriodicStack {

	public static void main(String[] args) {

		RtThread rt = new RtThread(10, 100000) {
			public void run() {

				waitForNextPeriod();
				int ts_old = Native.rd(Const.IO_US_CNT);

				for (;;) {
					waitForNextPeriod();
					int ts = Native.rd(Const.IO_US_CNT);
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
			Timer.wd();
			try { RtThread.sleepMs(1200); } catch (Exception e) {}
		}
	}
}
