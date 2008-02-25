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
import joprt.RtThread;

public class AudioTest
{
	static int pcm = 0;
	static int logFrequency = 0;
	
	public static void main(String[] args)
	{		
		new RtThread(10, 61)
		{
			public void run()
			{
				int counter = 0;
				boolean toggle = false;
				
				while (true)
				{
					if (((((1<<(13-logFrequency)))-1) & counter) == 0)
					{
						Speaker.write(toggle ? pcm : 0);					
						toggle = !toggle;
					}
							
					waitForNextPeriod();
					counter++;
				}
			}
		};
		
		new RtThread(10, 100*1000)
		{
			public void run()
			{
				int lastpcmPercentage = pcm; 
				int lastlogFrequency =  logFrequency;
				
				while(true)
				{
					if (Buttons.getButton(1))
					{
						pcm += 5;
					}
					if (Buttons.getButton(0))
					{
						pcm -= 5;
					}
					
					if (Buttons.getButton(3))
					{
						logFrequency += 1;
					}
					if (Buttons.getButton(2))
					{
						logFrequency -= 1;
					}
						
					logFrequency = Math.max(0, Math.min(13, logFrequency));
					pcm = Math.max(0, Math.min(255, pcm));
					
					if (lastpcmPercentage != pcm)
					{
						System.out.print("PCM: ");
						System.out.println(pcm);
					}
					lastpcmPercentage = pcm;
					
					if (lastlogFrequency!= logFrequency)
					{
						System.out.print("Frequency: ");
						System.out.println(((((1<<(13-logFrequency)))-1)));
						System.out.println(logFrequency);
					}
					lastlogFrequency = logFrequency;
					
					waitForNextPeriod();
				}
			}
		};
		
		RtThread.startMission();
	}
}
