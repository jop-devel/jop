/*
  This file is part of JOP, the Java Optimized Processor
    see <http://www.jopdesign.com/>

  Copyright (C) 2001-2008, Martin Schoeberl (martin@jopdesign.com)

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

/*
	Author: Tórur Biskopstø Strøm (torur.strom@gmail.com)
*/
package org.reprap;

import javax.realtime.PeriodicParameters;
import javax.realtime.PriorityParameters;
import javax.realtime.RelativeTime;
import javax.realtime.ThrowBoundaryError;
import javax.safetycritical.ManagedMemory;
import javax.safetycritical.Mission;
import javax.safetycritical.MissionSequencer;
import javax.safetycritical.PeriodicEventHandler;
import javax.safetycritical.Safelet;
import javax.safetycritical.StorageParameters;
import javax.safetycritical.JopSystem;
import com.jopdesign.io.*;

public class SerialController extends PeriodicEventHandler
{

	LedSwitchFactory LSF = LedSwitchFactory.getLedSwitchFactory();
	LedSwitch LS = LSF.getLedSwitch();
	char[] chars = new char[32];
	// First int contains number and second contains the length of number in char[]
	int[] intParseResults = new int[2];
	int charcnt = 0;
	boolean cmdRdy = false;
	
	SerialController()
	{
		super(new PriorityParameters(1),
			  new PeriodicParameters(null, new RelativeTime(5,0)),
			  new StorageParameters(10, null, 0, 0));
	}
	
	@Override
	public void handleAsyncEvent()
	{
		charcnt = 0;
		try
		{
			boolean charGarbage = false;
			char character = '0';
			while(System.in.available() != 0)
			{
				character = (char)System.in.read();
				System.out.print(character);
				if(character == '\n' || character == '\r' || character == ';')
				{
					// Empties the serial buffer for comments, end of line characters, etc.
					while(System.in.available() != 0)
					{
						System.in.read();
					}
					if(charcnt > 0)
					{
						LS.ledSwitch = 0x000000F0;
						parseCommand(chars,charcnt);
					}
				}
				if(charcnt < 31)
				{
					chars[charcnt] = character;
					charcnt++;
				}
			}
		}
		catch(Exception e)
		{
			System.out.print("ERROR:");
		}
	
	}
	
	private void parseCommand(char[] command, int length)
	{
		if(length > command.length)
		{
			System.out.println("Illegal parseCommand call!");
			LS.ledSwitch = 0x00000002;
			return;
		}
		if(command[0] != 'N')
		{
			System.out.println("Unknown command!");
			LS.ledSwitch = 0x00000004;
			return;
		}
		if(!parseInt(command,1,length,intParseResults))
		{
			resendCommand(0);
			LS.ledSwitch = 0x00000008;
			return;
		}
		int lineNumber = intParseResults[0];
		int checksum = 0;
		int i;
		for(i = 0; i < length && command[i] != '*'; i++)
		{
			checksum = checksum ^ command[i];
		}
		if(length-i < 1)
		{
			System.out.print(length);
			System.out.print(":");
			System.out.print(i);
			resendCommand(lineNumber);
			LS.ledSwitch = 0x00000010;
			return;
		}
		if(!parseInt(command,i,length-i,intParseResults))
		{
			resendCommand(lineNumber);
			LS.ledSwitch = 0x00000011;
			return;
		}
		if(intParseResults[0] != checksum)
		{
			resendCommand(lineNumber);
			LS.ledSwitch = 0x00000012;
			return;
		}
		LS.ledSwitch = 0x000000FF;
	}
	
	private void resendCommand(int lineNumber)
	{
		System.out.print("rs ");
		System.out.print(lineNumber);
		System.out.print("\r\n");
	}
	
	// From the Integer class but modified for char[]
	private static boolean parseInt(char[] chars, int startIndex, int stopIndex, int[] results)
	{
		int index = startIndex;
		boolean isNeg = false;
		int ch = chars[index];
		if (ch == '-')
		{
			if (stopIndex - startIndex <= 1)
				return false;
			isNeg = true;
			ch = chars[++index];
		}
		int radix = 10;
		int val = 0;
		while (index < stopIndex)
		{
			ch = Character.digit(chars[index], radix);
			val = val * radix + ch;
			if (ch < 0)
			{
				if(index == 0 || (isNeg && index == 1))
				{
					return false;
				}
				//stop when a non digit is found
				break;
			}
			index++;
		}
		results[0] = isNeg ? -val : val;
		results[1] = index;
		return true;
	}
}