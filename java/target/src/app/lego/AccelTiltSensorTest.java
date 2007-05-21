package lego;

import joprt.RtThread;
import lego.lib.*;

public class AccelTiltSensorTest
{
	public static void main(String[] args)
	{
		while (true)
		{
			System.out.print(Sensors.readSensor(0));
			System.out.print("  ");
			System.out.println(Sensors.readSensor(1));
			RtThread.sleepMs(100);
		}
	}
}
