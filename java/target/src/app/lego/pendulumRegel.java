package lego;

import joprt.RtThread;
import util.Timer;

import com.jopdesign.sys.Const;
import com.jopdesign.sys.Native;

public class pendulumRegel {
	static final int MAX_SPEED = 10;
	static final int sensorMask = ~(0x3<<9);
	static final int btnMask = 0x3<<9;
//	static final int T = 0x0001;
//	static final int Kpr = 1<<0;
//	static final int Ki = 0x0010;
static final int Ki = 1;
static final int Kd = 10;
static final int Kp = 1;
	static Motor left, right;
	static int soll, e, esum, last_e, stell, last_stell, tmp, i, speed, counter;
	static int[] data;
	static boolean forward, backward, btn1, btn2, stop, debounce;
	
	public static void init() {
		debounce = false;
		stop = true;
		i = 0;
		e = 0;
		last_e = 0;
		esum = 0;
		soll = -1;
		counter = 0;

		left = new Motor(0);
		right = new Motor(1);
		speed = 0;
		
		Timer.wd();
		last_stell = 0;
		tmp = 0;
	}
	
	public static void loop() {
		int val = Native.rd(Const.IO_LEGO); 
		int ist = val&sensorMask;
		i = i++ % 50;
		
	//	sensor -= 285;
	//		sensor = 0;
	//	} else {
	//		sensor *= 277;
	//		sensor >>>= 8;
	//	}
	//	if (sensor > 180) sensor = 180;
		
		int btn = (val&btnMask)>>9;
		btn1 = !((btn&0x1)==1);
		btn2 = !((btn&0x2)==2);
		
		if (soll == -1) {
			soll = ist;
		//	System.out.println("soll: " + soll);
		}
		
		if (btn2) {		// STOP
			forward = false;
			stop = true;
			esum = 0;
	//		left.stop();
	//		right.stop();
			soll = ist;
		//	System.out.println("soll: " + soll);
		 }
		
		if (btn1) { // STOP
			
					stop = false;
				
		}
		
		 
		 // Regelkreis
		 
		 e = ist-soll;
		 esum += e;
		 
	//	 stell = ((esum*1) + (Kpr*e) + last_stell) >> 8;

		/*
    if (e > 0) {
     if (e < 2)
      e = 0;
    } else
     if (e > -2)
      e = 0;
      
      */
		 
		 // static final int Kp = 100;
		 // static final int Kd = 20;
		 // static final int Ki = 1;
		// stell = Kp*e + Ki*esum + Kd*(e-last_e);
		
		
		// stell = Kp*e + (Ki*esum>>4); 1 50 /100
		
		// stell = (Kp*e + Kd*(e-last_e) + (Ki*esum>>10))>>6;
	 	stell = Kp*e;
		 //Ki*esum>>6)
		 
		 last_stell = stell;  
		 last_e = e; 
		

		 
		if (stell < 0) {
			stell = -stell;
			speed = stell;
			forward = false;
			backward = true;
		} else
		if (stell > 0) {
			speed = stell;
			backward = false;
			forward = true;	
		} else {
			speed = 0;
			forward = false;
			backward = false;
		}
		
		 if (stop)
		 speed = 0;
		
		if (speed < 2)
		 speed = 0;
		else
		 speed += 4;
		if (speed > 10)
		 speed = 10;
		
	if ((tmp++ % 200) == 1) {
		System.out.println("");
     System.out.println("e: " + e + "stell: " + stell + " speed: " + speed);
     System.out.println("");
//     System.out.println("Kp*e: " + Kp*e + " Kd*e-last_e: " + Kd*(e-last_e) + " Ki*esum>>10: " + (Ki*esum>>10));
     System.out.println("");
	}
 }
		
		
	public static void main(String[] args) {


		System.out.println("Go Go Balance!");
				
		init();
		
		
		new RtThread(10, 100*10) {
			public void run() {
				for (;;) {
					loop();
			//		RtThread.sleepMs(50);
					waitForNextPeriod();
				}
			}
		};

		RtThread.startMission();

		for (;;) {	  

		counter++;
		counter=counter%10;

		if (forward) {
				if ((counter >= speed)&&!(speed == MAX_SPEED)) {
				left.stop();
				right.stop();
				//off
			} else {
				right.forward();
				left.forward();
				//on
			}
			  
		} else
		if (backward) {
			if ((counter >= speed)&&!(speed == MAX_SPEED)) {
				left.stop();
				right.stop();
				//off
			} else {
				right.backward();
				left.backward();
				//on
			}
		} else {
			left.stop();
			right.stop();
	 }

			
		}

	}

}
