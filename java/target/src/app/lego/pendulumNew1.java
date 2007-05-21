package lego;

import joprt.RtThread;
import lego.lib.*;

//import util.Timer;
//import com.jopdesign.sys.Const;
//import com.jopdesign.sys.Native;

public class pendulumNew1 {
	static final int Ki = 1;
	static final int Kp = 1<<8;
	static final int Kd = 1<<16;
	static final int N = 200;
	
	
	
	
	static Motor[] motors;	
	static StringBuffer output;
	
	static int startval, angle, esum, soll, ist, e, stell, last_e, last_stell, counter, regcounter, tmp, counterSum, mid, d;
	
	static boolean active = false;
	static boolean btn3, btn2, btn1, hold, refreshOutput, setstell;
	
	static final int RINGBUFFER_LENGTH = 1<<8; 
    static int[] eRingBuffer = new int[RINGBUFFER_LENGTH];
    static int values;
	
	static int eRingBufferIndex = 0;
	
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
					refreshOutput = (counter % 0xA00 == 0);
					
					++regcounter;
					setstell = (regcounter % 0x8 == 0);
					
					
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
				

				e = ist-soll;
				esum += e;
				e *= 2;
				
				values -= eRingBuffer[eRingBufferIndex % RINGBUFFER_LENGTH];
				values += e;
				eRingBuffer[eRingBufferIndex % RINGBUFFER_LENGTH] = e;
								
				++eRingBufferIndex;
				
				mid = values / RINGBUFFER_LENGTH;
				
				stell = Kp*e + Ki*esum + Kd*(mid-last_e);
				
				//stell = Kp*e + (Ki*esum)>>4 + (Kd*(Math.abs(last_e-e)))>>4;
				
			
			//		stell = Kp*mid + (Ki*esum) ;//+ (Kd*(Math.abs(last_e-mid)))>>4;
					

				if (refreshOutput) {
					output.append(" mid: ").append(mid);
					output.append(" p: ").append(Kp*mid);
					output.append(" i: ").append((Ki*esum)>>4);
					output.append(" d: ").append(Kd*(last_e-mid));
					output.append(" stell: ").append(stell);
				}
				
				if (setstell) {
					last_e = mid;
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
