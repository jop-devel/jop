package lego;

import joprt.RtThread;
import util.Timer;

import com.jopdesign.sys.Const;
import com.jopdesign.sys.Native;

public class pendulumTestBench {

	static Motor left, right;

	
	public static void init() {
	
		left = new Motor(Motor.IO_MOTORA);
		right = new Motor(Motor.IO_MOTORB);
		
		left.setMotor(Motor.STATE_FORWARD, 0, 0xFFF);
		right.setMotor(Motor.STATE_FORWARD, 0, 0xFFF);
	
	}
	
	public static void loop() {
	
		
	}
}
		
		
	public static void main(String[] agrgs) {


		System.out.println("Hello  damnit test program!");
				
		init();
		
		System.out.println("Motors running?");
	

	}

}
