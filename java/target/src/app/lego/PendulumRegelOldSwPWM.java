package lego;

import joprt.RtThread;
import util.Timer;

import com.jopdesign.sys.Const;
import com.jopdesign.sys.Native;

public class PendulumRegelOldSwPWM {
	static int val;
	static boolean up;
	
	public static void init() {
		val = 0x80;
		up = true;
	}
	
	public static void loop() {
		Native.wr(val, Const.IO_LEGO);
		RtThread.sleepMs(600);
		
		if (up){
		switch (val) {
			case 0x80: val = 0x100; break;
			case 0x100: val = 0x200; break;
			case 0x200: val = 0x400; break;
			case 0x400: {
					up = false;
					val = 0x200;
					 break;
				}
			default: val = 0x80; break;
		}
		} else {
			switch (val) {
				case 0x80: {
					up = true;
					val = 0x100;
					 break;
				}
				case 0x100: val = 0x80; break;
				case 0x200: val = 0x100; break;
				default: val = 0x80; break;
		    }
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
			loop();
			
		}

	}

}
