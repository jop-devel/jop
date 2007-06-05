package lego;

import lego.lib.*;

import java.io.*;

import joprt.RtThread;

/*
 * Typical deviations @ full speed
 * 450 450 450 452 450 553 ~450
 * 300 440 360 405 339 290 160-467
 * 
 * Deviations | Avg @ 0 speed
 * 3500			
 * 3500
 * 
 * Deviations | Avg @ full speed
 * 4500
 * 3600-4250
 * 
 * Longer buffer:
 * 
 * Deviations | Avg @ 1 speed (bug w/ 0)
 * 0			-16
 * 4801			
 * 
 * Deviations | Avg @ full speed
 * 0			-6 
 * 0			-6
 */
public class BackEMFAvgDeviation
{

	/**
	 * @param args
	 */
	public static void main(String[] args) throws IOException
	{
		while (true)
		{
			System.out.println("Starting...");

			Motor m0 = new Motor(0);
			m0.setMotorPercentage(Motor.STATE_FORWARD, true, 1);

			RtThread.busyWait(10000 * 1000);

			final int VALUE_COUNT = 1000;
			int[][] values = new int[VALUE_COUNT][2];

			for (int i = 0; i < VALUE_COUNT; i++)
			{			
				RtThread.busyWait(10000);
				values[i] = m0.readNormalizedBackEMF();
			}

			int avg[] = new int[] {0,0};
			for (int i = 0; i < VALUE_COUNT; i++)
				for (int j = 0; j < 2; j++)
					avg[j] += values[i][j];

			for (int j = 0; j < 2; j++)
				avg[j] /= VALUE_COUNT;

			int dev[] = new int[] {0,0};		
			for (int i = 0; i < VALUE_COUNT; i++) 
				for (int j = 0; j < 2; j++)
					dev[j] += Math.abs(values[i][j] - avg[j]);

			for (int j = 0; j < 2; j++)
				System.out.println("Sum of deviations: " + dev[j] + " Avg: " + avg[j]);		

			m0.setState(Motor.STATE_OFF);
		}
	}
}
