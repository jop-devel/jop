package lego;

import joprt.RtThread;
import lego.lib.Sensors;

/**
 * XXX 0.3%
 * @author Peter Hilber (peter.hilber@student.tuwien.ac.at)
 *
 */
public class TestSensorChanges
{
	protected static final int sensor = 1;
	
	public static void main(String[] args)
	{
		int counter = 0;
		int unequal = 0;
		while (true)
		{
			if (Sensors.readSensor(1) != Sensors.readSensor(1))
				++unequal;
			++counter;
			if ((counter % 100) == 0)
			{
				System.out.println(counter + ": " + unequal);
			}
			RtThread.sleepMs(10);
		}
	}
}
