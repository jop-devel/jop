package lego;

import joprt.RtThread;
import lego.lib.*;

public class InvertedPendulumAngleTest
{
	
	/**
	 * @param args
	 */
	public static void main(String[] args)
	{
		StringBuffer output = new StringBuffer();
		int lastSensor;
		
		while(true)
		{
			output.setLength(0);
			
			lastSensor = InvertedPendulumSettings.getAngle();
			
			output.append(lastSensor);

			System.out.println(output);
			RtThread.sleepMs(100);
		}

	}

}
