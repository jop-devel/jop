package testrt;
import util.*;
import joprt.*;
import com.jopdesign.sys.*;

//	das Problem beim Flash loeschen:
//	Thread wird RUNNING, aber ist nicht in der Liste
//	hiermit (noch) nicht nachvollzeihbar

public class ProblemSleep {

	public static void main(String[] args) {

		Dbg.initSer();				// use serial line for debug output

/*
		RtThread rt = new RtThread(10, 3000) {
			public void run() {
				for (;;) {
					waitForNextPeriod();
				}
			}
		};

*/

		Thread th = new Thread() {
			public void run() {
				for (;;) {
					Thread.sleep(100);
					Dbg.wr('.');
				}
			}
		};
		th.start();

		// RtThread.startMission();

		for (;;) {
			Timer.wd();
			Dbg.wr('m');
			for (int i=0; i<10; ++i) {
				try {
					Thread.sleep(60);
				} catch (Exception e) {}
			}
		}
	}

}
