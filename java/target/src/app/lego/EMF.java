package lego;

import joprt.RtThread;
import util.Timer;

import com.jopdesign.sys.Const;
import com.jopdesign.sys.Native;

public class EMF {

	static Motor left, right;

	public static void init() {
	

		left = new Motor(0);
		right = new Motor(1);
	
	}
	
	public static void loop() {
		left.open();
		right.open();
 }
		
		
	public static void main(String[] args) {


		System.out.println("Go Go Balance!");
				
		init();
		
		
		new RtThread(10, 100*10) {
			public void run() {
				for (;;) {
					loop();
					waitForNextPeriod();
				}
			}
		};

		RtThread.startMission();

		for (;;) {	  
			
		}

	}

}
