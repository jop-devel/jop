package testurt;
import util.Dbg;
import util.Timer;
import jopurt.*;

public class Periodic {

	public static void main(String[] args) {

		Dbg.initSer();				// use serial line for debug output

		RtUserThread rt = new RtUserThread(10, 100000) {
			public void run() {

				for (;;) {
					Dbg.wr('.');
					waitForNextPeriod();
				}
			}
		};

		RtUserThread rtx = new RtUserThread(9, 500000) {
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

		RtUserThread rts = new RtUserThread(8, 1000000) {
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
		RtUserThread.sleepMs(1000);
		Dbg.wr("after sleep\n");

		RtUserThread.startMission();

		Dbg.wr("after Start\n");

		// sleep
		for (;;) {
			Dbg.wr('M');
// RtThread.debug();
			Timer.wd();
			RtUserThread.sleepMs(1200);
		}
	}

}
