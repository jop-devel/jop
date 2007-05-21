package lego;

import joprt.RtThread;
import util.Timer;

import com.jopdesign.sys.Const;
import com.jopdesign.sys.Native;

public class CopyOfSensorTesting {
	static Motor left, right;
	static int startval;
	static boolean freeRoad, btn1, btn2, mode1;
	
	public static void init() {
		freeRoad = true;
		left = new Motor(0);
		right = new Motor(1);
		mode1 = true;
		Timer.wd();
		left.ledOn();
	}
	
	public static void loop() {
		int sensor = Native.rd(Const.IO_LEGO+0); 
		int btn = Native.rd(Const.IO_LEGO+0x5); 

		System.out.println("sensor: " + sensor);

		btn1 = !((btn&0x1)==1);
		btn2 = !((btn&0x2)==2);
		
	
		if (btn1) {
		  left.ledOn();  // MODE 1
		 }
		 

		if (btn2) {
		 left.ledOff();
		}
		
		/*
		if (mode1) {
			freeRoad = sensor > 385;
		} else {
			freeRoad = ((sensor < startval + 10) && (sensor > startval -10));
			Timer.wd();
			left.ledBlink();
		}
		
		if (freeRoad) {	   	
		//	left.forward();
		//  right.forward();
			
		} else {
			Timer.wd();
			
			System.out.println(sensor);

		//	left.stop();
		//	right.stop();
			
		//	RtThread.sleepMs(1000);
			
		//	left.backward();
		//	right.backward();
			
		//	RtThread.sleepMs(1000);
			
		//	left.backward();
		//	right.forward();
			
			RtThread.sleepMs(1000); 
			Timer.wd();
		}*/
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
			  RtThread.sleepMs(500);
		}

	}

}
