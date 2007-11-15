package testrt;
import joprt.RtThread;
import joprt.SwEvent;
import util.Dbg;

public class Event {

	public final static int CNT = 10;
	static SwEvent sev;

	public static void main(String[] args) {

		sev = new SwEvent(2, 10000) {

			public void handle() {
				System.out.println("fire!");
			}
		};

		RtThread rt = new RtThread(1, 100000) {
			public void run() {

				int i;

				for (i=0; i<CNT; ++i) {
					waitForNextPeriod();
					System.out.println("befor");
					sev.fire();
					System.out.println("after");
				}
			
				for (;;) waitForNextPeriod();
			}
		};

		RtThread.startMission();

		for (;;) {
			;			// busy do nothing
		}
	}

}
