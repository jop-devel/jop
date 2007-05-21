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

public class pendulumNew4 {
		
	static Motor[] motors;	
	static StringBuffer output;
	
	public static final int ALED0 = 1<<1; 	
	public static final int ALED1 = 1<<3;
	public static final int ALED2 = 1<<5;
	public static final int ALED3 = 1<<7;
	static final int N = 200;
	
	static final int ANL = -3; 
	static final int ANM = -2;
	static final int ANS = -1;
	static final int APS = 1;
	static final int APM = 2;
	static final int APL = 3;
	
	static final int FNL = -50000; 
	static final int FNM = -30000;
	static final int FNS = -10000;
	static final int FPS = 10000;
	static final int FPM = 17000;
	static final int FPL = 50000;
	
	static final int SNL = -2; 
	static final int SNM = -1;
	static final int SPM = 1;
	static final int SPL = 2;
	
	static int startval, soll, ist, stell, counter, counterSum, e, esum, last_sum, mid, dcounter, anglevel;
	static int ns, nl, nm, pl, pm, ps;
	static int[] values;
	
	static boolean active = false;
	static boolean btn3, btn2, btn1, din0, din1, hold, refreshOutput;
	
	public static void init() {
		output = new StringBuffer();
		motors = new Motor[]
		{
				new Motor(InvertedPendulumSettings.MOTOR0),
				new Motor(InvertedPendulumSettings.MOTOR1)
		};

		anglevel = 0;
		mid = 0;
		startval = -1;
		hold = true;
		e = 0;
		soll = -1;
		counter = 0;
		counterSum = 0;
		values = new int[N];
		dcounter = 0;
	}


	public static void main(String[] agrgs) {
		System.out.println("Initializing...");
	
	

		init();


		new RtThread(10, 1000*1000)
		{			
			public void run()
			{
				while (active) {
					btn1 = Buttons.getButton(1);
					btn2 = Buttons.getButton(2);
					btn3 = Buttons.getButton(3);
					
					din0 = DigitalInputs.getDigitalInput(0);
					din1 = DigitalInputs.getDigitalInput(1);
					
					if ((!din0)||(!din1)) {
						hold = true;
					}
					
					++counter;
					refreshOutput = (counter % 0x100 == 0);
					dcounter++;
					dcounter %= 10;
					
					if (refreshOutput)
						output.setLength(0);
					
					if (btn1) { // RUN
						hold = false;
					}
					
						if (btn2) {		// STOP
						motors[0].setState(Motor.STATE_OFF);
						hold = true;
						motors[1].setState(Motor.STATE_OFF);
					 }
						
					if (btn3) {		// RESET START VALUE, START AGAIN
					  motors[0].setState(Motor.STATE_OFF);
					  motors[1].setState(Motor.STATE_OFF);
					  esum = 0;
					  soll = -1;
					}
					
				ist = Sensors.readSensor(InvertedPendulumSettings.TILT_SENSOR0);
				

				
				if (refreshOutput)
				output.append(" ist: ").append(ist);
				
				if (soll == -1)
					soll = ist;
				
				e = ist-soll;
				
				values[counterSum] = e;
				counterSum++;
				counterSum %= N;	
				
				mid = 0;
				for (int i = 0; i<N ; i++) {
					mid += values[i];
	
				}
				mid /= N;
				
				if (dcounter == 0) {
					anglevel = mid - last_sum;
					last_sum = mid;
				}
				
				if (refreshOutput)
					output.append(" mid: ").append(mid);

				
				if (anglevel == 0) {
					ps = FPS;
					pm = FPM;
					pl = FPL;
					ns = FNS;
					nm = FNM;
					nl = FNL;
				}
				if (anglevel >= SPM) {
					nl = FNM;
					nm = FNS;
					ns = FPS;
					ps = FPM;
					pm = FPL;
					pl = FPL;
				}
				if (anglevel >= SPL) {
					nl = FNS;
					nm = FPS;
					ns = FPM;
					ps = FPL;
					pm = FPL;
					pl = FPL;
				}
				if (anglevel <= SNM) {
					nl = FNL;
					nm = FNL;
					ns = FNM;
					ps = FNS;
					pm = FPS;
					pl = FPM;
				}
				if (anglevel <= SNL) {
					nl = FNL;
					nm = FNL;
					ns = FNL;
					ps = FNM;
					pm = FNS;
					pl = FPS;
				}				
				
				if (mid > 0) {
					stell = 1;
					if (mid > APS) {
						stell = ps; 
					}
					if (mid > APM) {
						stell = pm; 
					}
					if (mid > APL) {
						stell = pl; 
					}
				} else
				if (mid < 0) {
					stell = -1;
					if (mid < ANS) {
						stell = ns;
					}
					if (mid < ANM) {
						stell = nm;
					}
					if (mid < ANL) {
						stell = nl;
					}	
				} else
				if (mid == 0) {
					stell = 0;
				}
				

				
				if (refreshOutput) {
				output.append(" e: ").append(e);
				output.append(" anglevelocity: ").append(anglevel);
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
