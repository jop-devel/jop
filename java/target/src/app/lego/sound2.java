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

public class sound2 {
		
	static boolean flag;

	static int value, counter;
	
	public static void init() {
	  flag = false;	
	  value = 1;
	}


	public static void main(String[] agrgs) {
		System.out.println("Initializing...");
	
	

		init();


		new RtThread(10, 10 * 1000) {
			public void run() {
				while (true) {
					while (DigitalInputs.getDigitalInput(2)) {
						
						
						if (Buttons.getButton(0)) {
							while (Buttons.getButton(0) == true) ;
							value++;
						}
						if (Buttons.getButton(1)) {
							while (Buttons.getButton(1) == true) ;
							value--; 
						}

						
						
							if (flag) {
								Speaker.write(true);
								flag = false;
							} else {
								flag = true;
								Speaker.write(false);
							}
						
						RtThread.sleepMs(value);

						
					}
				}
			}
		};


		RtThread.startMission();
		
	}
	
	

}
