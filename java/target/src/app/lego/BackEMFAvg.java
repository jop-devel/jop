/*
  This file is part of JOP, the Java Optimized Processor
    see <http://www.jopdesign.com/>

  Copyright (C) 2007, Peter Hilber and Alexander Dejaco

  This program is free software: you can redistribute it and/or modify
  it under the terms of the GNU General Public License as published by
  the Free Software Foundation, either version 3 of the License, or
  (at your option) any later version.

  This program is distributed in the hope that it will be useful,
  but WITHOUT ANY WARRANTY; without even the implied warranty of
  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  GNU General Public License for more details.

  You should have received a copy of the GNU General Public License
  along with this program.  If not, see <http://www.gnu.org/licenses/>.
*/

package lego;

import lego.lib.*;

import java.io.*;

import joprt.RtThread;

public class BackEMFAvg
{

	/**
	 * @param args
	 */
	public static void main(String[] args) throws IOException
	{
		System.out.println("Starting...");
		
		Motor m0 = new Motor(0);
		m0.setMotor(Motor.STATE_FORWARD, true, 0);
		int pwmValue = 0;

		final int VALUE_COUNT = 10;
		int[][] values = new int[VALUE_COUNT][2];
		int index = 0;

		while (true)
		{			
			if (Buttons.getButton(0))
				pwmValue = Math.max(0, pwmValue-1);
			if (Buttons.getButton(1))
				pwmValue = Math.min(0x3f, pwmValue+1);
			if (Buttons.getButton(3))
				m0.setMeasure(true);
			if (Buttons.getButton(2))
				m0.setMeasure(false);

			m0.setDutyCycle((pwmValue * Motor.MAX_DUTYCYCLE) / 0x3f);
			Leds.setLeds(pwmValue >> 2);

			RtThread.busyWait(25 * 1000);

			int backEMF[] = m0.readBackEMF();

			values[index] = backEMF;

			int avg[] = new int[] {0,0};
			for (int i = 0; i < VALUE_COUNT; i++)
				for (int j = 0; j < 2; j++)
					avg[j] += values[i][j];

			if (index == VALUE_COUNT-1)
			{
				System.out.print("DC: ");
				System.out.print(pwmValue);
				System.out.print(" bEMF: ");
			}
			for (int j = 0; j < 2; j++)
			{
				avg[j] /= VALUE_COUNT;
				if (index == VALUE_COUNT-1)
				{
					System.out.print(avg[j] - 0x100);
					System.out.print(" ");
				}
			}
			if (index == VALUE_COUNT-1)
			{
				System.out.println();
			}
			

			index = (index + 1) % VALUE_COUNT;
		}
	}

}
