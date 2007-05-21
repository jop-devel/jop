package lego;

import joprt.RtThread;
import lego.lib.Buttons;
import lego.lib.DigitalInputs;
import lego.lib.FutureUse;
import lego.lib.Leds;
import lego.lib.Motor;
import lego.lib.Sensors;

//import util.Timer;
//import com.jopdesign.sys.Const;
//import com.jopdesign.sys.Native;

public class crashAvoider {
		
	static Motor[] motors;	
	static StringBuffer output;
	
	public static final int ALED0 = 1<<1; 	
	public static final int ALED1 = 1<<3;
	public static final int ALED2 = 1<<5;
	public static final int ALED3 = 1<<7;
	static final int SPEED = 75;
	
	
	static int sensor, startval;
	
	
	static boolean active = false;
	static boolean btn3, btn2, btn1, btn0,  din0, din1, hold, refreshOutput;
	
	public static void init() {
		output = new StringBuffer();
		motors = new Motor[]
		{
				new Motor(InvertedPendulumSettings.MOTOR0),
				new Motor(InvertedPendulumSettings.MOTOR1),
				new Motor(2)
		};


		hold = true;
	}
	 
	public static void wenden() {
		System.out.println("wenden.");
		
		
		setValue(0, 0);
		setValue(0, 1);
		setValue(0, 2);
		RtThread.sleepMs(1000);

		setValue(-1*SPEED, 0);			// zurück
		setValue(-1*SPEED, 1);
		RtThread.sleepMs(2000);
		
		
		setValue(-1*(SPEED-12), 1);				// rad drehen
		
		setValue(100, 2);
		RtThread.sleepMs(300);
		
		setValue(0, 2);
		RtThread.sleepMs(1000);
		

		setValue(-100, 2);
		RtThread.sleepMs(300);
		
		setValue(SPEED, 0);
		setValue(SPEED, 1);
		setValue(0, 2);	

		
	}


	public static void main(String[] agrgs) {
		System.out.println("Initializing...");
	
	

		init();


		new RtThread(10, 1000*1000)
		{			
			public void run()
			{
				while (active) {
					btn0 = Buttons.getButton(0);
					btn1 = Buttons.getButton(1);
					btn2 = Buttons.getButton(2);
					btn3 = Buttons.getButton(3);
					
					din0 = DigitalInputs.getDigitalInput(0);
					din1 = DigitalInputs.getDigitalInput(1);
					
					if ((!din0)||(!din1)) {
						
					}
					
					sensor = Sensors.readSensor(2);
					
					if (btn0) {
						startval = sensor;
						System.out.println(sensor);
					}
					
					if (!hold) {
						if (Math.abs(startval - sensor) > 10) {
							wenden();
						}
					} else {
						setValue(0, 0);			
						setValue(0, 1);
					}
					

					if (btn1) { // RUN
						
					}
					
					if (btn2) {	
						hold = true;
						setValue(0, 0);			
						setValue(0, 1);
					 }
						
					if (btn3) {		// RESET START VALUE, START AGAIN
						hold = false;
						motors[0].setState(Motor.STATE_FORWARD);
						motors[1].setState(Motor.STATE_FORWARD);
						setValue(SPEED, 0);			
						setValue(SPEED, 1);
					}
					
					
					
					

				
				}
			}
		};

		while (Buttons.getButtons() == 0);
		while (Buttons.getButtons() != 0);
		
		active = true;
		RtThread.startMission();
		
	}
	
	static void setValue(int value, int motor) {
		motors[motor].setMotorPercentage(value >= 0 ? Motor.STATE_FORWARD : Motor.STATE_BACKWARD, 
				true, Math.abs(value));
	}

}
