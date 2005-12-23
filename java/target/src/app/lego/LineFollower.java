package lego;

import joprt.RtThread;
import util.Timer;

import com.jopdesign.sys.Const;
import com.jopdesign.sys.Native;

public class LineFollower {

	final static int MAX = 1000;

	public static void main(String[] agrgs) {


		System.out.println("Hello LEGO world!");
				
		new RtThread(10, 100*1000) {
			public void run() {

				Motor left = new Motor(0);
				Motor right = new Motor(1);

				boolean black = false;


				for (;;) {
					int val = Native.rd(Const.IO_LEGO);
					black = val>380;
										
					if (black) {
						right.forward();
						left.stop();
					} else {
						left.forward();
						right.stop();
					}
					waitForNextPeriod();
				}
			}
		};

		RtThread.startMission();

		for (;;) {
			int val = Native.rd(Const.IO_LEGO);
			System.out.println(val);
			RtThread.sleepMs(500);
		}

	}

}
