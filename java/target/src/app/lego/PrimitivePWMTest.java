package lego;

import lego.lib.*;
import java.io.*;

import joprt.RtThread;

public class PrimitivePWMTest
{

	/**
	 * @param args
	 */
	public static void main(String[] args) throws IOException
	{
		Motor m0 = new Motor(0);
		m0.setMotor(Motor.STATE_FORWARD, true, 0);
		int value = 0;
		//int oldValue = -1;
		int backEMF;
		//int oldbackEMF = Integer.MIN_VALUE;
		
		while (true)
		{
			if (Buttons.getButton(0))
				value = Math.max(0, value-1);
			if (Buttons.getButton(1))
				value = Math.min(0x3f, value+1);
			if (Buttons.getButton(3))
				value = 0x3f;
			if (Buttons.getButton(2))
				value = 0;
			/*if (oldValue != value)
			{
				System.out.print("PWM: ");
				System.out.println(value);
			}
			oldValue = value;*/
			
			m0.setDutyCycle((value * Motor.MAX_DUTYCYCLE) / 0x3f);
			Leds.setLeds(value >> 2);
			RtThread.busyWait(75 * 1000);
			
			backEMF = m0.readBackEMF()[1];
				//if (Math.abs(backEMF - oldbackEMF) > 15)
				//{
					System.out.print("backEMF: ");
					System.out.println(backEMF - 0x100);
				//}
			//oldbackEMF = backEMF;
		}
	}

}
