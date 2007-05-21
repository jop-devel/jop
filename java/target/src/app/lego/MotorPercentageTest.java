package lego;

import lego.lib.*;
import java.io.*;

import joprt.RtThread;

public class MotorPercentageTest
{

	/**
	 * @param args
	 */
	public static void main(String[] args) throws IOException
	{
		Motor m0 = new Motor(2);
		m0.setMotor(Motor.STATE_FORWARD, true, 0);
		int value = 0;
		int oldValue = -1;
		
		while (true)
		{
			if (Buttons.getButton(0))
				value = Math.max(0, value-2);
			if (Buttons.getButton(1))
				value = Math.min(100, value+2);
			if (Buttons.getButton(3))
				value = 100;
			if (Buttons.getButton(2))
				value = 0;
			if (oldValue != value)
			{
				System.out.print("PWM: ");
				System.out.println(value);
			}
			oldValue = value;
			
			m0.setDutyCyclePercentage(value);
			Leds.setLeds(value >> 3);
			RtThread.busyWait(75 * 1000);
		}
	}

}
