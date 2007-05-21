package lego;

import joprt.RtThread;
import util.Timer;

import com.jopdesign.sys.Const;
import com.jopdesign.sys.Native;

public class pendulumTest {
	static final int sensorMask = ~(0x3<<9);
	static final int btnMask = 0x3<<9;
	static Motor left, right;
	static int startval, b1, speed, bounce, m1;
	static boolean forward, backward, btn1, btn2, hold, freeze, wait;
	
	public static void init() {
		freeze = false;
		startval = -1;
		hold = true;
		left = new Motor(0);
		right = new Motor(1);
		Timer.wd();
		left.ledOn();
		speed = 0;
		b1 = 0;
		bounce = 0;
		wait = false;
		m1 = 0;
	}
	
	public static void loop() {
		int val = Native.rd(Const.IO_LEGO); 
		int sensor = val&sensorMask;
		int btn = (val&btnMask)>>9;
		btn1 = !((btn&0x1)==1);
		btn2 = !((btn&0x2)==2);
		
		if (btn1 & !wait) { // STOP
			forward = true;
			freeze = false;
			m1 = 0;
			if (b1 == 0) {
				System.out.println("speed=1");
				wait = true;
				speed = 1;
				b1++;
			} else if (b1 == 1) {
				System.out.println("speed=2");
				wait = true;
				speed = 2;
				b1++;
			} else if (b1 == 2) {
				System.out.println("speed=3");
				wait = true;
				speed = 3;
				b1++;
			} else if (b1 == 3) {
				System.out.println("speed=4");
				wait = true;
				speed = 4;
				b1++;
			} else if (b1 == 4) {
				System.out.println("speed=5");
				wait = true;
				speed = 5;
				b1 = 0;
			}
		}
		
		if (wait) {
			bounce++;
			if (bounce > 50000) {
				bounce = 0;
				wait = false;
				System.out.println("done debouncing.");
			}
		}
		
		// LOGIC
		
		if (forward) {
			hold = false;
			
			if (speed == 0) {
				left.stop();
				right.stop();
			} else
			if (speed == 1) {
					
					if (m1 == 0) {
						left.forward();
						right.forward();
						m1++;
					} else if (m1 == 1) {
						left.stop();
						right.stop();
						m1++;
					} else if (m1 == 2) {
						m1++;
					} else if (m1 == 3) {
						m1++;
					} else if (m1 == 4) {
						m1++;
					} else if (m1 == 5) {
						m1++;
					} else {
						m1 = 0;
					}
			} else 
			if (speed == 2){
				
			} else
				forward = false;
			}
	}
		
			
		
		


	public static void main(String[] agrgs) {


		System.out.println("Hello LEGO world!");
				
		init();
		
	/*	new RtThread(10, 100*1000) {
			public void run() {
				for (;;) {
					loop();
					waitForNextPeriod();
				}
			}
		};*/

		RtThread.startMission();

		for (;;) {
			loop();
    	//	int val = Native.rd(Const.IO_LEGO); 
  		//	int sensor = val&sensorMask;
  		//	System.out.println("Sensor: " + sensor);
			  //RtThread.sleepMs(100);
		}

	}

}
