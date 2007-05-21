package lego;

import joprt.RtThread;
import lego.lib.Buttons;
import lego.lib.Motor;
import lego.lib.Sensors;

public class MotorTest
{

	/**
	 * @param args
	 */
	public static void main(String[] args)
	{
		System.out.println("Alive.");

		Motor[] motors = { new Motor(0), new Motor(1) };

		int richtwert;
		
		while (Buttons.getButtons() == 0)
		{
			//RtThread.sleepMs(100);
		}
		
		richtwert = Sensors.readSensor(2);
		
		for (int i = 0; i <= 1; i++)
		{
			motors[i].setMotor(Motor.STATE_BACKWARD, true, Motor.MAX_DUTYCYCLE / 2);
		}
		
		while (true)
		{
			int sensor = Sensors.readSensor(2);
			Motor.synchronizedReadBackEMF();
			System.out.println("Sensor: " + sensor + " Motor 0 back-emf: " + 
					motors[0].getSynchronizedBackEMF()[0] + ", " + motors[0].getSynchronizedBackEMF()[1]);
			if (Math.abs(sensor - richtwert) > 15)
			{
				for (int i = 0; i <= 1; i++)
				{
					motors[i].setState(Motor.STATE_FORWARD);
				}
				
				RtThread.sleepMs(500);
				
				motors[0].setState(Motor.STATE_BACKWARD);
				
				RtThread.sleepMs(1000);
			}
			else
			{
				motors[1].setState(Motor.STATE_BACKWARD);
			}
			
			RtThread.sleepMs(100);
		}
	}

}
