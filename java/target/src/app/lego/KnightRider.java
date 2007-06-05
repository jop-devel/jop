package lego;

import joprt.RtThread;
import lego.lib.FutureUse;

public class KnightRider {
	public static final int LED0 = 1<<1; 	
	public static final int LED1 = 1<<7;
	public static final int LED2 = 1<<5;
	public static final int LED3 = 1<<4;
	static int val;
	static boolean up;
	
	public static void init() {
		val = LED0;
		up = true;
	}
	
	public static void loop() {
		FutureUse.writePins(val);
		//Native.wr(val, IO_LEDS);
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
