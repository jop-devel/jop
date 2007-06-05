package lego;

import joprt.RtThread;
import lego.lib.*;

public class BackEMFTest
{
	/**
	 * @param args
	 */
	public static void main(String[] args)
	{
		new RtThread(10, 100*1000) {
			public void run()
			{
				Motor m0 = new Motor(0);
				Motor m1 = new Motor(1);

				m0.setMeasure(true);
				m1.setMeasure(true);

				while (true)
				{
					Motor.synchronizedReadBackEMF();
					int[] sBackEMF0 = m0.getSynchronizedBackEMF();
					int[] sBackEMF1 = m1.getSynchronizedBackEMF();

					System.out.println(
							sBackEMF0[0] + ", " + sBackEMF0[1] + "; " + sBackEMF1[0] + ", " + sBackEMF1[1]);

					waitForNextPeriod();
				}
			}

		};

		RtThread.startMission();
	}

}
