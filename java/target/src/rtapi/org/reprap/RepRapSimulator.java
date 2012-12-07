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
package org.reprap;

public class RepRapSimulator 
{
	private int X = 0;
	private int Y = 0;
	private int Z = 0;
	private int E = 0;
	
	private int temperature = 24*1000;
	
	int count = 0;
	
	public void write(int IOValue)
	{
		if((IOValue & (1 << 6)) > 0) // pulse X
		{
			if((IOValue & (1 << 8)) > 0)
			{
				X++;
			}
			else
			{
				X--;
			}
		}
		if((IOValue & (1 << 12)) > 0) // pulse Y
		{
			if((IOValue & (1 << 16)) > 0)
			{
				Y++;
			}
			else
			{
				Y--;
			}
		}
		if((IOValue & (1 << 20)) > 0 && (IOValue & (1 << 26)) > 0) // pulse Z
		{
			if((IOValue & (1 << 22)) > 0 && (IOValue & (1 << 28)) > 0)
			{
				Z--;
			}
			else
			{
				Z++;
			}
		}
		if((IOValue & (1 << 0)) > 0) // pulse E
		{
			if((IOValue & (1 << 2)) > 0)
			{
				E++;
			}
			else
			{
				E--;
			}
		}
		
		if((IOValue & (1 << 25)) > 0)
		{
			temperature++;
		}
		else if(temperature > 24000)
		{
			temperature--;
		}
		count++;
		if(count == 1000)
		{
			//System.out.println("X:"+X+" Y:"+Y+" Z:"+Z+" E:"+E);
			count = 0;
		}
	}
	
	public int readSensors()
	{
		int value = 168;
		if(X == 0)
		{
			value = value & ~(1 << 7);
		}
		if(Y == 0)
		{
			value = value & ~(1 << 5);
		}
		if(Z == 0)
		{
			value = value & ~(1 << 3);
		}
		return value;
	}
	
	public int readTemperature()
	{
		return temperature/1000;
	}
}
