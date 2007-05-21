package lego;

import joprt.RtThread;
import lego.lib.*;

//import util.Timer;
//import com.jopdesign.sys.Const;
//import com.jopdesign.sys.Native;

/**
 * 
 * @author Alexander Dejaco (alexander.dejaco@student.tuwien.ac.at)
 *
 */

public class sound3 {
		
	static boolean flag, up, on;
	static final int MAX = 50;
	static final int MIN = 10;
	static int value, counter, counter1, SPEED;
	
	static final int E = 15;
	static final int F = 14;
	static final int H = 20;
	static final int G = 12;
	static final int C = 19;

	static final int D = 17;

	static final int A = 22; 
	static final int A1 = 11; 
	
	public static void init() {
	  on = true;
	  up = true;
	  flag = false;	
	  value = 10;
	  SPEED = 0x100;
	}
	
	public static void tetris () {
		on = true;
		RtThread.sleepMs(4);
		on= false;
		RtThread.sleepMs(796);
		on = true;
		RtThread.sleepMs(4);
		on= false;
		RtThread.sleepMs(796);
		on = true;
		RtThread.sleepMs(4);
		on= false;
		RtThread.sleepMs(796);
		on = true;
		RtThread.sleepMs(4);
		on = false;
		RtThread.sleepMs(796);
		on = true;
		
		value = E;
		RtThread.sleepMs(400);
		value = H;
		RtThread.sleepMs(200);
		value = C;
		RtThread.sleepMs(200);
		value = D;
		RtThread.sleepMs(400);
		value = C;
		RtThread.sleepMs(200);
		value = H;
		RtThread.sleepMs(200);
		value = A;
		RtThread.sleepMs(600);
		value = C;
		RtThread.sleepMs(200);
		value = E;
		RtThread.sleepMs(400);
		value = D;
		RtThread.sleepMs(200);
		value = C;
		RtThread.sleepMs(200);
		value = H;
		RtThread.sleepMs(600);
		value = C;
		RtThread.sleepMs(200);
		value = D;
		RtThread.sleepMs(400);
		value = E;
		RtThread.sleepMs(400);
		value = C;
		RtThread.sleepMs(400);
		value = A;
		RtThread.sleepMs(400);
		on = false;
		RtThread.sleepMs(10);
		on = true; 
		RtThread.sleepMs(800);
		
		RtThread.sleepMs(200);
		value = D;
		RtThread.sleepMs(400);
		value = F;
		RtThread.sleepMs(200);
		value = A1;
		RtThread.sleepMs(400);
		value = G;
		RtThread.sleepMs(200);
		value = F;
		RtThread.sleepMs(200);
		value = E;
		RtThread.sleepMs(600);
		value = C;
		RtThread.sleepMs(200);
		value = E;
		RtThread.sleepMs(400);
		value = D;
		RtThread.sleepMs(200);
		value = C;
		RtThread.sleepMs(200);
		value = H;
		RtThread.sleepMs(600);
		value = C;
		RtThread.sleepMs(200);
		value = D;
		RtThread.sleepMs(400);
		value = E;
		RtThread.sleepMs(400);
		value = C;
		RtThread.sleepMs(400);
		value = A;
		RtThread.sleepMs(400);
		on = false;
		RtThread.sleepMs(10);
		on = true; 
		RtThread.sleepMs(800);
		
		on = false;
	}
	
	public static void loop() {
		if (on) {

			counter++;

			if ((counter % value) == 0) {
				if (flag) {
					flag = false;
				} else {
					flag = true;
				}
			}

			if (flag) {
				Speaker.write(true);
			} else
				Speaker.write(false);

		}
	}


	public static void main(String[] agrgs) {
		System.out.println("Initializing...");
	
	

		init();
		
		
		new RtThread(10, 1 * 100) {
			public void run() {
				for (;;) {
				loop();
				waitForNextPeriod();
				}
			}
		};
		
		
		
		RtThread.startMission();
		
		while(true) {
			on = true;
			tetris();	
			while (Buttons.getButtons() == 0);
			while (Buttons.getButtons() != 0);
		}
		
		
	}
	
	

}
