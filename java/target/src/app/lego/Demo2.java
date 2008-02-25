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

import com.jopdesign.sys.Const;
import com.jopdesign.sys.Native;

import joprt.RtThread;
import lego.lib.*;
import lego.lib.Motor;
/**
 * This program had been written for a specific robot we built for demonstration purposes
 *
 */
public class Demo2
{
	// configuration

	public static final boolean READ_BUTTON = true;
	public static final boolean READ_IR = false;

	// implementation

	public static final int IR_SENSOR = 2;
	public static final int IR_SENSOR_THRESHOLD = 10;

	public static Motor[] MOTORS;

	public static final int MOTOR_LEFT = 0;
	public static final int MOTOR_RIGHT = 1;

	public static final int DIFFERENCE_BUFFER_LENGTH = 3;

	public static final int STATE_OFF = 0;
	public static final int STATE_LINEFOLLOWER = 1;
	public static final int STATE_TOUCHSENSOR = 2;
	public static final int STATE_IRSENSOR = 3;
	public static final int STATE_ALL = 4;

	public static int state = STATE_OFF;


	// Knight Rider
	public static final int LED0 = 1<<1; 	
	public static final int LED1 = 1<<3;
	public static final int LED2 = 1<<5;
	public static final int LED3 = 1<<7;

	static int val, counter, value, counter1, freeValue;
	static boolean up, flag, speaker_up, stop;

	// Sound1		
	public static int speed = 0x100;

	/**
	 * @param args
	 */
	public static void main(String[] args)
	{
		new RtThread(10, 1*100)
		{
			public void run() {
				up = true;
				flag = false;	
				value = 10;
				speed = 0x400;
				while (true)
				{
					if (state == STATE_ALL)
					{
						counter++;
						if ((counter % value) == 0) {
							if (flag) {
								flag = false;
							} else {
								flag = true;
							}
						}

						counter1++;

						if ((counter1 % speed) == 0) {
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
					}
					else
					{
						flag = false;
					}

					if (flag) {
						Speaker.write(true);
					} else
						Speaker.write(false);


					waitForNextPeriod();
				}
			}
		};
		
		new RtThread(10, 100*1000) {
			public void run() {
				val = LED0;
				up = true;

				while (true)
				{
					if (state == STATE_ALL)
					{
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
					else if (state != STATE_LINEFOLLOWER)
					{
						val = 0;
					}

					FutureUse.writePins(val);
					waitForNextPeriod();
				}
			}
		};


		new RtThread(10, 50*1000)
		{
			public void run()
			{
				int forwardCount = 0;

				System.out.println("Main thread ready.");

				MOTORS = new Motor[] { new Motor(0), new Motor(1) };

				freeValue = 0;
				stop = true;

				while (true)
				{


					for (int i = 0; i < 4; i++)
						if (Buttons.getButton(i))
						{
							state = i+1;
							//System.out.println(state);
							Leds.setLed(1, (i & 1) != 0);
							Leds.setLed(2, (i & 2) != 0);
							break;
						}

					stop = !DigitalInputs.getDigitalInput(2);

					if ((Buttons.getButtons() != 0) || stop)
						freeValue = Sensors.readSensor(IR_SENSOR);

					if (stop)
						for (int i = 0; i < 2; i++)
							MOTORS[i].setState(Motor.STATE_OFF);
					
//					System.out.print(MOTORS[0].readNormalizedBackEMF()[0]);
//					System.out.print(" ");
//					System.out.print(MOTORS[0].readNormalizedBackEMF()[1]);
//					System.out.println();

					switch (state)
					{
						case STATE_OFF:
						{
							for (int i = 0; i < 2; i++)
								MOTORS[i].setState(Motor.STATE_OFF);
							break;
						}
						case STATE_LINEFOLLOWER:
						{							
							if (!stop)
							{
								int val = Sensors.readSensor(IR_SENSOR);
								//System.out.println(val);
								boolean black = val > 285; // XXX
								//boolean black = val > freeValue - 5;

								MOTORS[MOTOR_LEFT].setDutyCyclePercentage(60);
								MOTORS[MOTOR_RIGHT].setDutyCyclePercentage(60);

								if (black) {
									MOTORS[MOTOR_RIGHT].setState(Motor.STATE_FORWARD);
									MOTORS[MOTOR_LEFT].setState(Motor.STATE_BRAKE);
									FutureUse.writePins(LED0 | LED3);
								} else {
									MOTORS[MOTOR_LEFT].setState(Motor.STATE_FORWARD);
									MOTORS[MOTOR_RIGHT].setState(Motor.STATE_BRAKE);
									FutureUse.writePins(LED1 | LED2);
								}
							}

							break;
						}
						case STATE_TOUCHSENSOR:
						case STATE_IRSENSOR:
						case STATE_ALL:
						{
							//System.out.println(stop ? "stop" : "!stop");

							if (!stop)
							{								
								for (int i = 0; i < 2; i++)
									MOTORS[i].setMotorPercentage(Motor.STATE_FORWARD, true, 40);

								int totalDifference = Sensors.readSensor(IR_SENSOR) - freeValue;
								
								if (forwardCount >= 60)
								{
									if (MOTORS[0].readNormalizedBackEMF()[1] <= 5)
									{
										Leds.setLeds(0x9);
										turnback(false);
									}
								}
								
								if (((state == STATE_IRSENSOR)||(state == STATE_ALL)) && (Math.abs(totalDifference) >= IR_SENSOR_THRESHOLD))
								{
									turnback(true);
								} 
								else if ((state == STATE_TOUCHSENSOR || state == STATE_ALL) &&
										(forwardCount >= 6) &&
										(DigitalInputs.getDigitalInput(1)))
								{
									turnback(false);
								}
							}
							break;
						}
					}

					{
						boolean goingForward = false;						
						for (int i = 0; i < 2; i++)
						{
							if (MOTORS[i].getState() == Motor.STATE_FORWARD)
							{
								goingForward = true;
								break;
							}
						}

						forwardCount = goingForward ? forwardCount + 1 : 0;
					}

					waitForNextPeriod();
				}
			}

			void turnback(boolean fast) {
				int turnspeed, turnsleep;
				boolean dirLeft = false;

				if (fast)
				{
					turnspeed = 80;
					turnsleep = 300;
				} else
				{
					turnspeed = 70;
					turnsleep = 400;
				}

				speed = !fast ? 0x400 : 0x50 ;

				dirLeft = (Native.rd(Const.IO_US_CNT) & 1) != 0;
				//Leds.setLed(0, dirLeft);
				//Leds.setLed(3, !dirLeft);

				for (int i = 0; i < 2; i++)
					MOTORS[i].setMotorPercentage(Motor.STATE_BACKWARD,
							false, 100);

				RtThread.sleepMs(100);


				for (int i = 0; i < 2; i++)
					MOTORS[i].setMotorPercentage(Motor.STATE_BACKWARD,
							false, turnspeed);

				RtThread.sleepMs(turnsleep-100);

				MOTORS[dirLeft ? MOTOR_LEFT : MOTOR_RIGHT].setMotorPercentage(Motor.STATE_BACKWARD, false, 70);
				MOTORS[dirLeft ? MOTOR_RIGHT : MOTOR_LEFT].setState(Motor.STATE_OFF);

				RtThread.sleepMs(600);

				speed = 0x400;

				Leds.setLed(0, false);
				Leds.setLed(3, false);
			}

		};

		RtThread.startMission();
	}
}
