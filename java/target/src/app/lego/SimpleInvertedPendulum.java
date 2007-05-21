package lego;

import joprt.RtThread;
import lego.lib.*;

public class SimpleInvertedPendulum
{
	static Motor[] motors;	
	static StringBuffer output;

	static boolean active = true;

	/**
	 * @param args
	 */
	public static void main(String[] args)
	{
		output = new StringBuffer();
		motors = new Motor[] 
		                   {	new Motor(InvertedPendulumSettings.MOTOR0),
				new Motor(InvertedPendulumSettings.MOTOR1) };

		/*
		new RtThread(10, 1000*1000)
		{
			int counter = 0;

			public void run()
			{
				while (true)
				{
					if (++counter == 2)
						active = false;
					waitForNextPeriod();
				}
			}
		};*/

		new RtThread(10, 10*1000)
		{			
			public void run()
			{

				int counter = 0;
				boolean refreshOutput;

				while (active)
				{
					// emergency stop
					if (Buttons.getButtons() != 0)
						break;

					++counter;
					refreshOutput = (counter % 0x100 == 0);

					if (refreshOutput)
						output.setLength(0);

					int position = InvertedPendulumSettings.getAngle(); 		// 32.0
					int error = position;			//

					setLedsToError(error);
					
					//if (Math.abs(error) > 20)
					//	break;

					if (refreshOutput)
						output.append("position: ").append(position);


					setControlValue(error >= 0 ? - Motor.MAX_DUTYCYCLE : Motor.MAX_DUTYCYCLE);

					if (refreshOutput)
						System.out.println(output);

					waitForNextPeriod();
				}

				for (int i = 0; i < 2; i++)
					motors[i].setState(Motor.STATE_BRAKE);
			}
		};

		System.out.println("Ready to start...");

		while (Buttons.getButtons() == 0)
			setLedsToError(InvertedPendulumSettings.getAngle());
		while (Buttons.getButtons() != 0)
			setLedsToError(InvertedPendulumSettings.getAngle());
		RtThread.sleepMs(1000);

		RtThread.startMission();
	}


	protected static void setControlValue(int controlValue)
	{
		for (int i = 0; i<2; i++)
			motors[i].setMotor(controlValue >= 0 ? Motor.STATE_FORWARD : Motor.STATE_BACKWARD, 
					true, Math.max(0, Math.min(Motor.MAX_DUTYCYCLE, Math.abs(controlValue))));

	}
	
	protected static void setLedsToError(int error)
	{
		Leds.setLeds((Math.abs(error >> 2) & 0x7) << 1 | (error >= 0 ? 1: 0));
	}

}
