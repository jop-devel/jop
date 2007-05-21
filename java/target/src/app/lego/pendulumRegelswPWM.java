package lego;

import joprt.RtThread;
import util.Timer;

import com.jopdesign.sys.Const;
import com.jopdesign.sys.Native;

public class pendulumRegelswPWM {
	static final int sensorMask = ~(0x3<<9);
	static final int btnMask = 0x3<<9;
	static Motor left, right;
	static int startval, b1, speed, bounce, m1, tmp;
	static boolean forward, backward, btn1, btn2, start;
	
	public static void init() {
		startval = -1;
		left = new Motor(0);
		right = new Motor(1);
		Timer.wd();
		left.ledOn();
		speed = 0;
		b1 = 0;
		bounce = 0;
		m1 = 0;
		start = false;
		tmp = 0;
	}
	
	public static void loop() {
		int val = Native.rd(Const.IO_LEGO); 
		int sensor = val&sensorMask;
		
		
//		sensor -= 285;
	//	if (sensor<0) {
//			sensor = 0;
//		} else {
//			sensor *= 277;
//			sensor >>>= 8;
//		}
//		if (sensor > 180) sensor = 180;
		
	//	sensor *= 288;
	//	sensor >>>= 8;
	//	if (sensor<0) sensor=0;
	//	if (sensor>180) sensor=180;
		
		int btn = (val&btnMask)>>9;
		btn1 = !((btn&0x1)==1);
		btn2 = !((btn&0x2)==2);
		
		if (startval == -1) {
			startval = sensor;
			System.out.println("startval: " + startval);
		}
		
		if (btn2) {		// STOP
			forward = false;
			speed = 0;
			left.stop();
			right.stop();
			startval = sensor;
		 }
		
		if (btn1) { // STOP
			start = true;		
		}
		
		if ((tmp++ % 10000) == 0) {
				System.out.println(sensor);
/	 }
		
		if (start) {
		if (sensor > startval - 15) {
			forward = true;
			backward = false;
			speed = 5;
		} else if (sensor > startval - 10) {
			forward = true;
			backward = false;
			speed = 3;
		} else if (sensor > startval - 5) {
			forward = true;
			backward = false;
			speed = 1;
		} else if (sensor < startval + 15) {
			backward = true;
			forward = false;
			speed = 5;
		} else if (sensor < startval + 10) {
			backward = true;
			forward = false;
			speed = 3;
		} else if (sensor < startval + 5) {
			backward = true;
			forward = false;
			speed = 1;
		} else {
			forward = false;
			backward = false;
			speed = 0;
		}
		
		
		// LOGIC
		
		if (speed == 0) {
			left.stop();
			right.stop();
		} else 
		if (speed == 1){
			if (m1 == 0) {
				if (forward) {
					left.forward();
					right.forward();
				} else {
					left.backward();
					right.backward();
				}
				m1++;
			} else if (m1 == 1) {
				
				m1++;
			} else if (m1 == 2) {
				m1++;
			} else if (m1 == 3) {
				left.stop();
				right.stop();
				m1++;
			} else if (m1 == 4) {
				m1++;
			} else if (m1 == 5) {
				m1++;
				
			} else {
				m1 = 0;
			}
		} else
		if (speed == 5) {
			if (m1 == 0) {
				if (forward) {
					left.forward();
					right.forward();
				} else {
					left.backward();
					right.backward();
				}
				m1++;
			} else if (m1 == 1) {
			
				m1++;
			} else if (m1 == 2) {
				m1++;
			} else if (m1 == 3) {
				m1++;
			} else if (m1 == 4) {
				m1++;
			} else if (m1 == 5) {
				m1++;
			//	left.stop();
			//	right.stop();
			} else {
				m1 = 0;
			}
		} else 
		if (speed == 3) {
			if (m1 == 0) {
				if (forward) {
					left.forward();
					right.forward();
				} else {
					left.backward();
					right.backward();
				}
				m1++;
			} else if (m1 == 1) {
				
				m1++;
			} else if (m1 == 2) {
				m1++;
			} else if (m1 == 3) {
			
				m1++;
			} else if (m1 == 4) {
				left.stop();
				right.stop();
				m1++;
			} else if (m1 == 5) {
				m1++;
				
			} else {
				m1 = 0;
			}
		} else
		{
			left.stop();
			right.stop();
		}
	
		}
	}
		
		
	public static void main(String[] agrgs) {


		System.out.println("Hello LEGO world!");
				
		init();
		
	/*	new RtThread(10, 100) {
			public void run() {
				for (;;) {
					loop();
					waitForNextPeriod();
					RtThread.sleepMs(100);
				}
			}
		};*/

//		RtThread.startMission();

		for (;;) {
			loop();
			
		}

	}

}
