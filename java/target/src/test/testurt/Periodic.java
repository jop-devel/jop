package testurt;
import util.*;
import joprt.*;
import com.jopdesign.sys.Native;

public class Periodic {

	public static void main(String[] args) {

		Dbg.initSer();				// use serial line for debug output

		RtThread rt = new RtThread(10, 100000) {
			public void run() {

				for (;;) {
					Dbg.wr('.');
					waitForNextPeriod();
				}
			}
		};

		RtThread rtx = new RtThread(9, 500000) {
			public void run() {

				for (;;) {
					Dbg.wr('+');
					waitForNextPeriod();
				}
			}
		};

		//
		// do busy work
		//

		RtThread rts = new RtThread(8, 1000000) {
			public void run() {
				for (;;) {
					Dbg.wr('*');
					int ts = Scheduler.getNow() + 990000;
					while (ts-Scheduler.getNow()>0)
						;
					waitForNextPeriod();
				}
			}
		};

		Dbg.wr("befor Start\n");
		RtThread.sleepMs(1000);
		Dbg.wr("after sleep\n");

		RtThread.startMission();

		Dbg.wr("after Start\n");

		// sleep
		for (;;) {
			Dbg.wr('M');
// RtThread.debug();
			Timer.wd();
			RtThread.sleepMs(1200);
		}
	}

}
