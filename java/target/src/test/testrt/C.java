package testrt;
import joprt.RtThread;
import util.Dbg;

public class C {

	private static Object monitor;

	public static void main(String[] args) {

		Dbg.initSer();				// use serial line for debug output

		monitor = new Object();

		Thread th = new Thread() {
			public void run() {
				work('a');
			}
		};
		th.start();

		RtThread rth = new RtThread(10, 80000) {
			public void run() {
				for (;;) {
					Dbg.wr('$');
					waitForNextPeriod();
				}
			}
		};

		RtThread.startMission();

		work('0');
	}

	static void work(int ch) {

		for (;;) {

			synchronized (monitor) {
				for (int i=0; i<5; ++i) {
					for (int j=0; j<386000/5; ++j) ;
					Dbg.wr(ch+i);
				}
			}
			for (int i=5; i<10; ++i) {
				for (int j=0; j<386000/5; ++j) ;
				Dbg.wr(ch+i);
			}
		}
	}

}
