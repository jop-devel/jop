package lego;

import joprt.RtThread;
import lego.lib.*;

public class InvertedPendulum
{
	static final int I_MAX = 1 << 7; //(Motor.MAX_DUTYCYCLE << 8) >> 1;		// 32.0
	static final int I_MIN = -I_MAX;		// 32.0	
	static final int P_GAIN = (Motor.MAX_DUTYCYCLE << 8) >> 4;	// 0.32
	static final int I_GAIN = (Motor.MAX_DUTYCYCLE << 8) >> 7;	// 0.32
	static final int D_GAIN = (Motor.MAX_DUTYCYCLE << 8) >> 6;	// 0.32

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

		new RtThread(10, 2000*1000)
		{
			public void run()
			{
				active = false;
			}
		};
		
		new RtThread(10, 10*1000)
		{			
			public void run()
			{
				int pTerm;				// 24.8
				int iTerm = 0;			// 24.8
				int dTerm;				// 24.8

				int iState = 0;			// 32.0
				int lastPosition = 0;	// 24.8

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
					
					//if (Math.abs(error) > 20)
					//	break;

					if (refreshOutput)
						output.append("position: ").append(position);

					pTerm = P_GAIN * error;			// 24.8

					iState = Math.max(I_MIN, Math.min(I_MAX, iState + error));
					iTerm = I_GAIN * iState;	// 32.0

					dTerm = D_GAIN * (position - lastPosition);
					lastPosition = position;

					if (refreshOutput)
					{
						output.append(" pTerm: ").append(pTerm >> 8);
						output.append(" iTerm: ").append(iTerm >> 8);
						output.append(" dTerm: ").append(dTerm >> 8);
					}

					int controlValue = (pTerm + iTerm - dTerm) >> 8; // 32.0

					if (refreshOutput)
						output.append(" controlValue: ").append(controlValue);

					setControlValue(controlValue);

					if (refreshOutput)
						System.out.println(output);

					waitForNextPeriod();
				}
				
				for (int i = 0; i < 2; i++)
					motors[i].setState(Motor.STATE_BRAKE);
			}
		};
		
		System.out.println("Ready to start...");
		
		while (Buttons.getButtons() == 0);
		while (Buttons.getButtons() != 0);
		RtThread.sleepMs(1000);
		
		RtThread.startMission();
	}


	protected static void setControlValue(int controlValue)
	{
		for (int i = 0; i<2; i++)
			motors[i].setMotor(controlValue >= 0 ? Motor.STATE_FORWARD : Motor.STATE_BACKWARD, 
					true, Math.max(0, Math.min(Motor.MAX_DUTYCYCLE, Math.abs(controlValue))));

	}

}
