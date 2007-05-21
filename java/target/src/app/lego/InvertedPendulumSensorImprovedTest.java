package lego;

import joprt.RtThread;
import lego.lib.*;

// XXX GC error
// XXX where is the memory lost?
public class InvertedPendulumSensorImprovedTest
{
	/*static final int AVERAGE_BUFFER_SIZE = 0x10;
	static final int AVERAGE_BUFFER_SIZE_LD = 3;*/

	static final int AVERAGE_BUFFER_SIZE = 0x1;
	static final int AVERAGE_BUFFER_SIZE_LD = 0;

	
	static int[][] movingAverage;
	
	/**
	 * @param args
	 */
	public static void main(String[] args)
	{		
		movingAverage = new int[2][AVERAGE_BUFFER_SIZE];
		
		new RtThread(10, 256)
		{
			public void run()
			{
				while (true)
				{
					for (int i = 0; i < AVERAGE_BUFFER_SIZE; i++)
						for (int j = 0; j < 2; j++)
						{
							movingAverage[j][i] = Sensors.readSensor(j == 0 ? InvertedPendulumSettings.TILT_SENSOR0 : InvertedPendulumSettings.TILT_SENSOR1) - InvertedPendulumSettings.TILT_SENSOR_BASE_VALUE - InvertedPendulumSettings.TILT_SENSOR_BALANCED_VALUE;
							waitForNextPeriod();
						}
				}
			}
		};

		new RtThread(10, 100 * 1000)
		{
			StringBuffer output = new StringBuffer(100);

			public void run()
			{


				while (true)
				{

					// calc moving average

					int mean[] = new int[] {0, 0};
					for (int j = 0; j < 2; j++)
						for (int i = 0; i < AVERAGE_BUFFER_SIZE; i++)
							mean[j] += movingAverage[j][i];

					for (int j = 0; j < 2; j++)
						mean[j] >>= AVERAGE_BUFFER_SIZE_LD;

					//System.out.println(com.jopdesign.sys.GC.freeMemory());
					output.setLength(0);
					output.append(mean[0]).append(" ").append(mean[1]);

					System.out.println(output);
					//System.out.println(com.jopdesign.sys.GC.freeMemory());

					waitForNextPeriod();
				}
			}
		};

		RtThread.startMission();
	}

}

