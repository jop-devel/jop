package lego;

import joprt.RtThread;
import lego.lib.*;

//import util.Timer;
//import com.jopdesign.sys.Const;
//import com.jopdesign.sys.Native;

public class pendulumNew1Bkup {
	static final int Ki = 1;
	static final int Kp = 1000000;
	static final int Kd = 2;
	
	
	static Motor[] motors;	
	static StringBuffer output;
	
	static int startval, angle, esum, soll, ist, e, stell, last_e, last_stell, counter, regcounter, tmp;
	
	static boolean active = false;
	static boolean btn3, btn2, btn1, hold, refreshOutput, setstell;
	
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
	}


	public static void main(String[] agrgs) {
		System.out.println("Initializing...");
	
	

		init();

		new RtThread(10, 10*1000)
		{			
			public void run()
			{
				while (active) {
					btn1 = Buttons.getButton(1);
					btn2 = Buttons.getButton(2);
					btn3 = Buttons.getButton(3);
					
					++counter;
					refreshOutput = (counter % 0x1000 == 0);
					
					++regcounter;
					setstell = true;//(regcounter % 0x100 == 0);
					
					
					if (refreshOutput)
						output.setLength(0);
					
					if (btn1) { // RUN
						hold = false;
					}
					
					if (btn2) {		// STOP
						motors[0].setState(Motor.STATE_OFF);
						motors[1].setState(Motor.STATE_OFF);
						hold = true;
					 }
					
					if (btn3) {		// RESET START VALUE, START AGAIN
					  motors[0].setState(Motor.STATE_OFF);
					  motors[1].setState(Motor.STATE_OFF);
					  
						e = 0;
						last_e = 0;
						esum = 0;
						soll = -1;
						last_stell = 0;
					}
					
			//	ist = InvertedPendulumSettings.getAngle();
					
				ist = Sensors.readSensor(InvertedPendulumSettings.TILT_SENSOR0);
				
				if (refreshOutput)
				output.append(" ist: ").append(ist);
				
				if (soll == -1)
					soll = ist;
				
				if (refreshOutput)
				output.append(" soll: ").append(soll);
				
				
				e = ist-soll;
				esum += e;
				
				if ((e < 10)&&(e > -10)) {
					Leds.setLed(0, true);
					Leds.setLed(1, false);
				
				if (esum > 0) {
					stell = 0;
					
	
					
					if (esum > 2500) {
						stell = 10000; 

					}
					if (esum > 3500) {
						stell = 17500; 

					}
					if (esum > 4000) {
						stell = 25000;
					}
				} else
					if (esum < 0) {
						stell = 0;

						
						if (esum < -2500) {
							stell = -10000;

						}
						if ( esum < -3500) {
							stell = -17500;

						}
						if (esum < -4000) {
							stell = -25000;

						}
					}
				} else
				
				
				{

					Leds.setLed(0, false);
					Leds.setLed(1, true);


	
				if (setstell)
					stell = Kp*e + (Ki*esum)>>2;
				stell = stell >> 4;
				if (stell > 0) {
					if (stell < 7000)
						stell = 7000;
				} else if (stell < 0) {
					if (stell > - 7000)
						stell = - 7000;					
				}
					

			//	stell = Kp*e;
				
				last_stell = stell;
				last_e = e;
				

				
				}
				if (e == 0)
				{
					stell = 0;
					esum = 0;
				}
				
				
				
				if (refreshOutput) {
				output.append(" e: ").append(e);
				output.append(" esum: ").append(esum);
				output.append(" stell: ").append(stell);
				
				}
				
				
				if (refreshOutput)
					System.out.println(output);

				
				if (!hold) {
					//setValue(stell*166116);
					//setValue(stell*10000);
					setValue(stell);
				//	setValue(stell >= 0 ? (stell < 3500 ? 3500 : stell) : (stell > -3000 ? -3000 : stell));
				} else
					setValue(0);
					
				}
			}
		};

		while (Buttons.getButtons() == 0);
		while (Buttons.getButtons() != 0);
		
		active = true;
		RtThread.startMission();
		
	}
	
	static void setValue(int value) {
		motors[0].setMotor(value >= 0 ? Motor.STATE_BACKWARD : Motor.STATE_FORWARD, 
				true, Math.max(0, Math.min(Motor.MAX_DUTYCYCLE, Math.abs(value))));
		motors[1].setMotor(value >= 0 ? Motor.STATE_BACKWARD : Motor.STATE_FORWARD, 
				true, Math.max(0, Math.min(Motor.MAX_DUTYCYCLE, Math.abs(value))));
	}

}
