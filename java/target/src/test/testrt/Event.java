package testrt;
import util.*;
import joprt.*;
import com.jopdesign.sys.Native;

public class Event {

	public final static int CNT = 10;
	static SwEvent sev;

	public static void main(String[] args) {

		Dbg.initSer();				// use serial line for debug output

		sev = new SwEvent(2, 10000) {

			public void handle() {
				Dbg.wr("fire!");
			}
		};

		RtThread rt = new RtThread(1, 100000) {
			public void run() {

				int i;

				for (i=0; i<CNT; ++i) {
					waitForNextPeriod();
					Dbg.wr("\nbefor");
					sev.fire();
					Dbg.wr("after");
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
