package lego;

import com.jopdesign.sys.*;
import joprt.RtThread;
import lego.lib.Buttons;
import lego.lib.DigitalInputs;
import lego.lib.FutureUse;
import lego.lib.Leds;
import lego.lib.Microphone;
import lego.lib.Motor;
import lego.lib.Sensors;
import lego.lib.Speaker;

/**
 * 
 * @author Peter Hilber (peter.hilber@student.tuwien.ac.at) and Alexander Dejaco (alexander.dejaco@student.tuwien.ac.at)
 *
 */
public class LegoBoardTest
{
	// configuration
	static final boolean REPEAT = true;
	static final int INTERVAL = 1000;

	static final boolean SPEAKER_DEMO = true;
	static final boolean BUTTONS = true;
	static final boolean DIGITALINPUTS = true;
	static final boolean FUTUREUSE = true;
	static final boolean LEDS = true;
	static final boolean MICROPHONE = true;
	static final boolean MOTORS = false;
	static final boolean SENSORS = true;
	static final boolean PLD_RAW_INPUT = true;
	static final boolean KNIGHT_RIDER_DEMO = true;
	static final boolean FREEMEMORY = true;

	public static final int LED0 = 1<<1; 	
	public static final int LED1 = 1<<3;
	public static final int LED2 = 1<<5;
	public static final int LED3 = 1<<7;

	static int val, counter, value, counter1;
	static boolean up, flag, speaker_up;

	public static void knightRiderLoop() {
		FutureUse.writePins(val);
		//Native.wr(val, IO_LEDS);

		if (up){
			switch (val) {
				case LED0: val = LED1; break;
				case LED1: val = LED2; break;
				case LED2: val = LED3; break;
				case LED3: {
					up = false;
					val = LED2;
					break;
				}
				default: val = LED0; break;
			}
		} else {
			switch (val) {
				case LED0: {
					up = true;
					val = LED1;
					break;
				}
				case LED1: val = LED0; break;
				case LED2: val = LED1; break;
				default: val = LED0; break;
			}
		}
	}
	
	public static void speakerLoop() {
		counter++;
		if ((counter % value) == 0) {
			if (flag) {
				flag = false;
			} else {
				flag = true;
			}
		}
		
		counter1++;
		
		if ((counter1 % 0x200) == 0) {
			if (speaker_up) {
				value++;
				if (value >= 50) {
					speaker_up = false;
					value--;
				}
			} else
			{
				value--;
				if (value <= 10) {
					speaker_up = true;
					value++;
				}
			}
		}

		if (flag) {
			Speaker.write(true);
		} else
			Speaker.write(false);
	}

	/**
	 * @param args
	 */
	public static void main(String[] args)
	{				
		System.out.println("Initializing.");
		
	/*	Motor.setMotor(0, Motor.STATE_FORWARD, true, Motor.MAX_DUTYCYCLE);
		Motor.setMotor(1, Motor.STATE_FORWARD, true, Motor.MAX_DUTYCYCLE);
		Motor.setMotor(2, Motor.STATE_FORWARD, true, Motor.MAX_DUTYCYCLE);
		//Native.wr(-1 << 1, Motor.IO_OUTPUT_MOTOR[1]);
*/
		if (KNIGHT_RIDER_DEMO)
		{
			val = LED0;
			up = true;

			new RtThread(10, 100*1000) {
				public void run() {
					for (;;) {
						knightRiderLoop();
						waitForNextPeriod();

					}
				}
			};
		}
		
		if (SPEAKER_DEMO) {
			  up = true;
			  flag = false;	
			  value = 10;
			  
			new RtThread(10, 1*100) {
				public void run() {
					for (;;) {
						speakerLoop();
						waitForNextPeriod();

					}
				}
			};
		}

		new RtThread(10, 1000*1000)
		{
			public void run()
			{
				StringBuffer output = new StringBuffer(500);
				do
				{					
					output.setLength(0);
					
					output.append("New measurement...\n\n");
					if (FREEMEMORY)
					{
						output.append("Free memory: ").append(GC.freeMemory()).append("\n");
					}
					if (BUTTONS)
					{
						for (int i = 0; i < 4; i++)
						{
							// uncomment this to have fun with javac
							//System.out.println("Button " + i + ": " + Buttons.getButton(i));
							// uncomment this to have fun with JOP
							//System.out.println("Button " + i + ": " + new Boolean(Buttons.getButton(i)));
							output.append("Button ").append(i).append(": ").append(Buttons.getButton(i) ? "Down" : "Up").append("\n");
						}
					}
					if (DIGITALINPUTS)
					{
						for (int i = 0; i < 3; i++)
							output.append("Digital input ").append(i).append(": ").append(DigitalInputs.getDigitalInput(i) ? "1" : "0").append("\n");
					}
					if (FUTUREUSE)
					{
						output.append("Unknown input: 0x").append(
								Integer.toHexString((FutureUse.readPins()))).append("\n");
					}
					if (LEDS)
					{
						Leds.setLeds(-1);
					}
					if (MICROPHONE)
					{
						output.append("Microphone: ").append(Microphone.readMicrophone()).append("\n");
					}
					if (MOTORS)
					{
						Motor.synchronizedReadBackEMF();
						for (int i = 0; i < 2; i++)
						{
							int[] backEMF = new Motor(i).getSynchronizedBackEMF();
							output.append(
									"Motor ").append(i).append(" back-emf measurement: ").append(backEMF[0] - 0x100).append(", ").append(backEMF[1] - 0x100).append("\n");
						}
					}			
					if (SENSORS)
					{
						Sensors.synchronizedReadSensors();
						for (int i = 0; i < 3; i++)
							output.append("Analog sensor ").append(i).append(": ").append(Sensors.getBufferedSensor(i)).append(
								" (").append(Sensors.readSensorValueAsPercentage(i)).append(("%)")).append("\n");
					}
					if (PLD_RAW_INPUT)
					{
						output.append("PLD raw input: ").append(Native.rd(Const.IO_LEGO + 7)).append("\n");
					}

					output.append("\nMeasurement finished.\n\n");
					//output.append("Length: ").append(output.length()).append(" Capacity: " ).append(output.capacity()).append("\n");
					
					System.out.print(output);
					if (!REPEAT)
						break;
					waitForNextPeriod();
				} while (true);
			}
		};

		RtThread.startMission();

		System.out.println("Started.");
	}

}
