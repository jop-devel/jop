/*
  Copyright (C) 2012, Tórur Biskopstø Strøm (torur.strom@gmail.com)

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

package com.jopdesign.io;

import org.reprap.Math;


public final class ExpansionHeader extends HardwareObject
{	
	public volatile int IO;
	public volatile int ADC;
	
	public void write(int IOValue)
	{
		IO = IOValue;
	}
	
	public int readSensors()
	{
		return IO;
	}
	
	public int readTemperature()
	{
		return timingToTemperature(ADC);
	}
	
	private static final int[] TIMING = {1693,2213,3016,4078,5877,8158,12822,20636,32302,55876,88901,175887,331095};
	private static final int[] TEMPERATURE = {209,197,187,163,150,138,117,99,85,68,56,38,22};
	
	private static int timingToTemperature(int timing)
	{
		if(timing <= 0)
		{
			return 0;
		}
		int i;
		for(i = 0; i < TIMING.length-2; i++) //@WCA loop = 11
		{
			if(timing <= TIMING[i])
			{
				break;
			}
		}
		return (TEMPERATURE[i]+Math.divs1000(Math.divs1000((timing-TIMING[i])*((1000000*(TEMPERATURE[i+1]-TEMPERATURE[i]))/(TIMING[i+1]-TIMING[i])))));
	}
	
}
