package lego;

import joprt.RtThread;
import util.Timer;

import com.jopdesign.sys.Const;
import com.jopdesign.sys.Native;

public class LineFollower {

	final static int MAX = 1000;
	static Motor left, right;
	static boolean black;
	
	public static void init() {
		
		left = new Motor(0);
		right = new Motor(1);

		black = false;
	}
	
	public static void loop() {

		int val = Native.rd(Const.IO_LEGO);
		black = val>380;
							
		if (black) {
			right.forward();
			left.stop();
		} else {
			left.forward();
			right.stop();
		}
	}

	public static void main(String[] agrgs) {


		System.out.println("Hello LEGO world!");
				
		init();
		
		new RtThread(10, 100*1000) {
			public void run() {
				for (;;) {
					loop();
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
