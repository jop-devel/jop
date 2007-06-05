package lego;

import com.jopdesign.sys.Const;
import com.jopdesign.sys.Native;

import joprt.RtThread;
import lego.lib.*;

/**
 * This program had been written for a specific robot we built for demonstration purposes
 *
 */
public class CrashAvoider2
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



	/**
	 * @param args
	 */
	public static void main(String[] args)
	{
//		int[] difference = new int[DIFFERENCE_BUFFER_LENGTH];
//		int differenceIndex = 0;

		new RtThread(10, 100*1000)
		{
			public void run()
			{
				System.out.println("Ready.");

				Leds.setLeds(~0);

				MOTORS = new Motor[] { new Motor(0), new Motor(1) };

				int freeValue;

				while (Buttons.getButtons() == 0)
				{
					System.out.println(Sensors.readSensor(IR_SENSOR));
				}

				freeValue = Sensors.readSensor(IR_SENSOR);

				boolean stop = true;

				System.out.println("Starting...");

				Leds.setLeds(0);

				while (true)
				{
//					if (Buttons.getButton(0))
//					{
//					freeValue = Sensors.readSensor(IR_SENSOR);
//					stop = true;
//					System.out.println(freeValue);
//					}

//					if (Buttons.getButton(3))
//					{
//					if (stop)
//					System.out.println("Restarting...");
//					stop = false;						
//					}

					stop = !DigitalInputs.getDigitalInput(2);
					if (stop)
						freeValue = Sensors.readSensor(IR_SENSOR);

					for (int i = 0; i < 2; i++)
						MOTORS[i].setMotorPercentage(stop ? Motor.STATE_OFF
								: Motor.STATE_FORWARD, false, 70);

//					difference[differenceIndex] = Sensors.readSensor(IR_SENSOR) - freeValue;
//					differenceIndex = (differenceIndex+1) % DIFFERENCE_BUFFER_LENGTH;

//					int totalDifference = 0;

//					for (int i = 0; i < DIFFERENCE_BUFFER_LENGTH; i++)
//					totalDifference += Math.abs(difference[i]);

					if (!stop)
					{

						int totalDifference = Sensors.readSensor(IR_SENSOR) - freeValue;

						boolean dirRight = false;

						if ((READ_BUTTON && DigitalInputs.getDigitalInput(1))
								|| (READ_IR && (Math.abs(totalDifference) >= IR_SENSOR_THRESHOLD)))
						{
							dirRight = (Native.rd(Const.IO_US_CNT) & 1) != 0;
							Leds.setLeds(dirRight ? 0x1 : 0x8);
						}

						if (READ_BUTTON && DigitalInputs.getDigitalInput(1))
						{
							for (int i = 0; i < 2; i++)
								MOTORS[i].setMotorPercentage(Motor.STATE_BACKWARD,
										false, 100);

							RtThread.sleepMs(300);

							MOTORS[dirRight ? MOTOR_LEFT : MOTOR_RIGHT].setState(Motor.STATE_OFF);
							MOTORS[dirRight ? MOTOR_RIGHT : MOTOR_LEFT].setMotorPercentage(Motor.STATE_BACKWARD, false, 100);

							RtThread.sleepMs(500);
							
							for (int i = 0; i < 2; i++)
								MOTORS[i].setState(Motor.STATE_OFF);
							
							RtThread.sleepMs(2000);
						}
						else if (READ_IR && (Math.abs(totalDifference) >= IR_SENSOR_THRESHOLD))
						{
							for (int i = 0; i < 2; i++)
								MOTORS[i].setMotorPercentage(Motor.STATE_BACKWARD,
										false, 80);

							RtThread.sleepMs(500);

							MOTORS[dirRight ? MOTOR_LEFT : MOTOR_RIGHT].setMotorPercentage(Motor.STATE_BACKWARD, false, 100);
							MOTORS[dirRight ? MOTOR_RIGHT : MOTOR_LEFT].setState(Motor.STATE_OFF);

							RtThread.sleepMs(500);
						}
						continue;
					}
					waitForNextPeriod();
				}
			}
		};

		RtThread.startMission();
	}
}
