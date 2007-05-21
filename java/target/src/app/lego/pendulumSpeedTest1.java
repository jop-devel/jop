package lego;

import joprt.RtThread;
import lego.lib.*;

//import util.Timer;
//import com.jopdesign.sys.Const;
//import com.jopdesign.sys.Native;

public class pendulumSpeedTest1 {
	static final int Ki = 1;
	static final int Kp = 10000;
	static final int Kd = 2;
	
	
	static Motor[] motors;	
	static StringBuffer output;
	
	static int startval, angle, esum, soll, ist, e, stell, last_e, last_stell, counter, regcounter, tmp;
	
	static boolean active = false;
	static boolean btn3, btn2, btn1, btn0, hold, refreshOutput, setstell;
	
	public static void init() {
		output = new StringBuffer();
		motors = new Motor[]
		{
				new Motor(InvertedPendulumSettings.MOTOR0),
				new Motor(InvertedPendulumSettings.MOTOR1)
		};
		
		startval = -1;
		hold = true;
		
		e = 0;
		last_e = 0;
		esum = 0;
		soll = -1;
		last_stell = 0;
		counter = 0;
		regcounter = 0;
		stell = 0;
	}


	public static void main(String[] agrgs) {
		System.out.println("Initializing...");
	
	

		init();

		new RtThread(10, 10*1000)
		{			
			public void run()
			{
				while (active) {
					btn0 = Buttons.getButton(0);
					btn1 = Buttons.getButton(1);
					btn2 = Buttons.getButton(2);
					btn3 = Buttons.getButton(3);
					

					output.setLength(0);
					
					
					if (btn2) { // +
						stell += 1;
						System.out.println(stell);
					}
					
					if (btn3) {		// -
						stell -= 1;
						System.out.println(stell);
					 }
					
					if (btn0) {		// RESET START VALUE, START AGAIN
						stell = 0;
						System.out.println(stell);
					}
					
				ist = InvertedPendulumSettings.getAngle();
				

				setValue(stell<<1);
				}
			}
		};

		while (Buttons.getButtons() == 0);
		while (Buttons.getButtons() != 0);
		
		active = true;
		RtThread.startMission();
		
	}
	
	static void setValue(int value) {
		motors[0].setMotor(value >= 0 ? Motor.STATE_FORWARD : Motor.STATE_BACKWARD, 
				true, Math.max(0, Math.min(Motor.MAX_DUTYCYCLE, Math.abs(value))));
		motors[1].setMotor(value >= 0 ? Motor.STATE_FORWARD : Motor.STATE_BACKWARD, 
				true, Math.max(0, Math.min(Motor.MAX_DUTYCYCLE, Math.abs(value))));
	}

}
