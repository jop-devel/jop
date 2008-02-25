/*
  This file is part of JOP, the Java Optimized Processor
    see <http://www.jopdesign.com/>

  Copyright (C) 2007, Peter Hilber and Alexander Dejaco

  This program is free software: you can redistribute it and/or modify
  it under the terms of the GNU General Public License as published by
  the Free Software Foundation, either version 3 of the License, or
  (at your option) any later version.

  This program is distributed in the hope that it will be useful,
  but WITHOUT ANY WARRANTY; without even the implied warranty of
  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  GNU General Public License for more details.

  You should have received a copy of the GNU General Public License
  along with this program.  If not, see <http://www.gnu.org/licenses/>.
*/

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
			  
			new RtThread(10, 1*1000) {
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
				do
				{					
					System.out.println("New measurement...");
					System.out.println();
					if (FREEMEMORY)
					{
						System.out.print("Free memory: ");
						System.out.println(GC.freeMemory());
					}
					if (BUTTONS)
					{
						for (int i = 0; i < 4; i++)
						{
							// uncomment this to have fun with javac
							//System.out.println("Button " + i + ": " + Buttons.getButton(i));
							// uncomment this to have fun with JOP
							//System.out.println("Button " + i + ": " + new Boolean(Buttons.getButton(i)));
							System.out.print("Button ");
							System.out.print(i);
							System.out.print(": ");
							System.out.println(Buttons.getButton(i) ? "Down" : "Up");
						}
					}
					if (DIGITALINPUTS)
					{
						for (int i = 0; i < 3; i++) {
							System.out.print("Digital input ");
							System.out.print(i);
							System.out.print(": ");
							System.out.println(DigitalInputs.getDigitalInput(i) ? "1" : "0");
			
						}
							//output.append("Digital input ").append(i).append(": ").append(DigitalInputs.getDigitalInput(i) ? "1" : "0").append("\n");
					}
					if (FUTUREUSE)
					{
						System.out.print("Unknown input: 0x");
						System.out.println(Integer.toHexString((FutureUse.readPins())));
					}
					if (LEDS)
					{
						Leds.setLeds(-1);
					}
					if (MICROPHONE)
					{
						System.out.print("Microphone: ");
						System.out.println(Microphone.readMicrophone());
					}
					if (MOTORS)
					{
						Motor.synchronizedReadBackEMF();
						for (int i = 0; i < 2; i++)
						{
							// MS: two times new to read the back EMF!!!
							int[] backEMF = new Motor(i).getSynchronizedBackEMF();
							System.out.print("Motor ");
							System.out.print(i);
							System.out.print(" back-emf measurement: ");
							System.out.print(backEMF[0] - 0x100);
							System.out.print(", ");
							System.out.println(backEMF[1] - 0x100);
						}
					}			
					if (SENSORS)
					{
						Sensors.synchronizedReadSensors();
						for (int i = 0; i < 3; i++) {
							System.out.print("Analog sensor ");
							System.out.print(i);
							System.out.print(" : ");
							System.out.print(Sensors.getBufferedSensor(i));
							System.out.print(" (");
							System.out.print(Sensors.readSensorValueAsPercentage(i));
							System.out.println("%)");
						}
					}
					if (PLD_RAW_INPUT)
					{
						System.out.print("PLD raw input: ");
						System.out.println(Native.rd(Const.IO_LEGO + 7));
					}

					System.out.println();
					System.out.println("Measurement finished.");
					//output.append("Length: ").append(output.length()).append(" Capacity: " ).append(output.capacity()).append("\n");
					
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
