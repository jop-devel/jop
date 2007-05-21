package lego;

import joprt.RtThread;

import com.jopdesign.sys.Const;
import com.jopdesign.sys.Native;

public class Rider {
	public static final int IO_LEDS = Const.IO_LEGO + 0;	
	public static final int LED0 = 0x01; 	
	public static final int LED1 = 0x02;
	public static final int LED2 = 0x04;
	public static final int LED3 = 0x08;
	static int val;
	static boolean up;
	
	public static void init() {
		val = LED0;
		up = true;
	}
	
	public static void loop() {
		Native.wr(val, IO_LEDS);
		RtThread.sleepMs(100);
		
		if (up){
		switch (val) {
			case LED0: val = LED1; break;
			case LED1: val = LED2; break;
			case LED2: val = LED3; break;
			case LED3: {
					up = false;
					val = LED2;
					 break;
				}
			default: val = LED0; break;
		}
		} else {
			switch (val) {
				case LED0: {
					up = true;
					val = LED1;
					 break;
				}
				case LED1: val = LED0; break;
				case LED2: val = LED1; break;
				default: val = LED0; break;
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
