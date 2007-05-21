package lego;

import lego.lib.Motor;

public class NonSynchronizedBackEMFRead
{
	/**
	 * @param args
	 */
	public static void main(String[] args)
	{
		int i = 0;
		Motor m0 = new Motor(0);
		m0.setMotor(Motor.STATE_FORWARD, true, Motor.MAX_DUTYCYCLE);
		int count = 0;

		while (true)
		{		
			// GC works :)
			++i;
			int a = m0.readBackEMF()[0];
			int b = m0.readBackEMF()[0];
			if (a != b)
			{
				System.out.print(++count);
				System.out.print("/");
				System.out.println(i);
			}
			/*System.out.print(m0.readBackEMF()[0]);
			System.out.print(" ");
			System.out.println(m0.readBackEMF()[0]);*/
			//RtThread.busyWait(100 * 1000);
		}
	}
}
