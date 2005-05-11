package testurt;
import joprt.RtThread;
import util.Dbg;
import util.Timer;

public class Two extends RtThread {

	int c;
	Two(int ch) {

		super(5, 100000);
		c = ch;
	}

	public void run() {

		for (;;) {
			Dbg.wr(c);
			waitForNextPeriod();
/*
					int ts = Native.rd(Native.IO_US_CNT) + 99000;
					while (ts-Native.rd(Native.IO_US_CNT)>0)
						;
*/
		}
	}
	

	public static void main(String[] args) {

		Dbg.initSer();				// use serial line for debug output

		new Two('a');
		new Two('b');
		new Two('c');

		RtThread.startMission();

		// sleep
		for (;;) {
			Dbg.wr('M');
			Timer.wd();
			RtThread.sleepMs(1200);
		}
	}

}
