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
