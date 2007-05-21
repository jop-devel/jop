package lego;

import lego.lib.*;
import java.io.*;

public class PWMTest
{

	/**
	 * @param args
	 */
	public static void main(String[] args) throws IOException
	{
		Motor m0 = new Motor(0);
		m0.setMotor(Motor.STATE_FORWARD, true, 0);
		
		while (true)
		{
			System.out.println("Reading...");
			// TODO write to System.in
			m0.setDutyCycle(
			(Integer.parseInt(new Byte((byte)System.in.read()).toString()) * Motor.MAX_DUTYCYCLE) / 9);
			System.out.println("Read.");
		}
	}

}
