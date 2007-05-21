package lego;

import joprt.RtThread;
import lego.lib.*;

public class InvertedPendulumSensorTest
{
	
	/**
	 * @param args
	 */
	public static void main(String[] args)
	{
		StringBuffer output = new StringBuffer();
		int[] lastSensor = new int[2];
		
		while(true)
		{
			output.setLength(0);
			
			lastSensor[0] = Sensors.readSensor(InvertedPendulumSettings.TILT_SENSOR0) - InvertedPendulumSettings.TILT_SENSOR_BASE_VALUE;
			lastSensor[1] = Sensors.readSensor(InvertedPendulumSettings.TILT_SENSOR1) - InvertedPendulumSettings.TILT_SENSOR_BASE_VALUE; 
			
			output.append(lastSensor[0]);
			output.append("   ");
			output.append(lastSensor[1]);
			// XXX memory
			System.out.println(output);
			RtThread.sleepMs(100);
		}

	}

}
