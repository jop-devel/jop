package lego;

import _legonotforrelease.MyRtThread;

import com.jopdesign.sys.Const;
import com.jopdesign.sys.Native;

import joprt.RtThread;
import lego.lib.*;
/**
 * This program had been written for a specific robot we built for demonstration purposes
 *
 */
public class Demo1
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
		new MyRtThread(10, 100*1000) {
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
					else
					{
						val = 0;
					}

					FutureUse.writePins(val);
					waitForNextPeriod();
				}


			}
		};

		new MyRtThread(10, 1*100)
		{
			public void run() {
				up = true;
				flag = false;	
				value = 10;
				speed = 0x100;

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


		new MyRtThread(10, 100*1000)
		{
			public void run()
			{
				System.out.println("Main thread ready.");

				MOTORS = new Motor[] { new Motor(0), new Motor(1) };

				freeValue = 0;
				stop = true;

				while (true)
				{


					for (int i = 0; i < 4; i++)
						if (Buttons.getButton(i))
						{
							state = i;
							Leds.setLed(1, (state & 1) != 0);
							Leds.setLed(2, (state & 2) != 0);
							break;
						}

					stop = !DigitalInputs.getDigitalInput(2);

					if ((Buttons.getButtons() != 0) || stop)
						freeValue = Sensors.readSensor(IR_SENSOR);


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
							break;
						}
						case STATE_TOUCHSENSOR:
						case STATE_IRSENSOR:
						case STATE_ALL:
						{
							for (int i = 0; i < 2; i++)
								MOTORS[i].setMotorPercentage(stop ? Motor.STATE_OFF
										: Motor.STATE_FORWARD, false, 70);
							if (!stop)
							{
								int totalDifference = Sensors.readSensor(IR_SENSOR) - freeValue;
								if (((state == STATE_IRSENSOR)||(state == STATE_ALL)) && (Math.abs(totalDifference) >= IR_SENSOR_THRESHOLD))
								{
									turnback(false);
								} 
								else if ((state == STATE_TOUCHSENSOR)&&(DigitalInputs.getDigitalInput(1)))
								{
									turnback(true);
								}					
							}
							break;
						}
					}

					waitForNextPeriod();
				}
			}

			void turnback(boolean fast) {
				int turnspeed, turnsleep;
				boolean dirRight = false;

				if (fast)
				{
					turnspeed = 80;
					turnsleep = 500;
				} else
				{
					turnspeed = 70;
					turnsleep = 1000;
				}

				speed += 0x50;

				dirRight = (Native.rd(Const.IO_US_CNT) & 1) != 0;
				Leds.setLed(0, dirRight);
				Leds.setLed(3, !dirRight);

				for (int i = 0; i < 2; i++)
					MOTORS[i].setMotorPercentage(Motor.STATE_BACKWARD,
							false, turnspeed);

				rtSleep(turnsleep);

				MOTORS[dirRight ? MOTOR_LEFT : MOTOR_RIGHT].setMotorPercentage(Motor.STATE_BACKWARD, false, 100);
				MOTORS[dirRight ? MOTOR_RIGHT : MOTOR_LEFT].setState(Motor.STATE_OFF);

				rtSleep(500);

				speed -= 0x50;
				
				Leds.setLed(0, false);
				Leds.setLed(3, false);
			}

		};

		RtThread.startMission();
	}
}
