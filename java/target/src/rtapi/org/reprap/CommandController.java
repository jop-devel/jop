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

public class CommandController extends PeriodicEventHandler
{
	private static final int DECIMALS = 3;
	private static CommandController instance;
	
	public static CommandController getInstance()
	{
		if(instance == null)
		{
			instance = new CommandController();
		}
		return instance;
	}
	
	private CommandController()
	{
		super(new PriorityParameters(1),
			  new PeriodicParameters(null, new RelativeTime(1,0)),
			  new StorageParameters(300, null, 0, 0));
	}
	
	private int lineNumber = 0;
	
	@Override
	public void handleAsyncEvent()
	{
		CharacterBuffer cb = CharacterBuffer.getReadyBuffer();
		if(cb == null)
		{
			//No commands to process
			return;
		}
		
		int index = 0;
		
		boolean seenNCommand = false;
		
		boolean seenGCommand = false;
		boolean seenMCommand = false;
		int commandNumber = -1;
		
		boolean seenXCommand = false;
		int XValue = 0;
		
		boolean seenYCommand = false;
		int YValue = 0;
		
		boolean seenZCommand = false;
		int ZValue = 0;
		
		boolean seenECommand = false;
		int EValue = 0;
		
		boolean seenFCommand = false;
		int FValue = 0;
		
		boolean seenSCommand = false;
		int SValue = 0;
		
		boolean seenStarCommand = false;
		int checksum = 0;
		
		
		while(index < cb.length)
		{
			char character = cb.chars[index];
			if(character != 'N' && character != 'G' && character != 'M' && character != 'X' 
					&& character != 'Y' && character != 'Z' && character != 'F'
					&& character != 'E' && character != 'S' && character != '*')
			{
				resendCommand("Incorrect command",lineNumber);
				return;
			}
			char command = character;
			index++;
			int numberLength = 0;
			int value = 0;
			boolean decimalpoint = false;
			int decimals = 0;
			while(index < cb.length)
			{
				character = cb.chars[index];
				
				if(Character.digit(character, 10) != -1)
				{
					value = value * 10 + character;
					numberLength++;
					if(decimalpoint)
					{
						decimals++;
						if(decimals > DECIMALS)
						{
							resendCommand("To many decimals in number",lineNumber);
							return;
						}
					}
					if((index + 1) == cb.length)
					{
						//End of command line
						break;
					}
				}
				else if(character == ' ' && numberLength > 0)
				{
					//Command delimiter
					break;
				}
				else if(character == '.')
				{
					decimalpoint = true;
				}
				else
				{
					resendCommand("Incorrect number in command",lineNumber);
					return;
				}
			}
			
			
			switch(command)
			{
				case 'N':
					lineNumber = value;
					seenNCommand = true;
					break;
				case 'G':
					commandNumber = value;
					seenGCommand = true;
					break;
				case 'M':
					commandNumber = value;
					seenMCommand = true;
					break;
				case 'X':
					XValue = value*(10^(DECIMALS-decimals));
					seenXCommand = true;
					break;
				case 'Y':
					YValue = value*(10^(DECIMALS-decimals));
					seenYCommand = true;
					break;
				case 'Z':
					ZValue = value*(10^(DECIMALS-decimals));
					seenZCommand = true;
					break;
				case 'E':
					EValue = value;
					seenECommand = true;
					break;
				case 'F':
					FValue = value;
					seenFCommand = true;
					break;
				case 'S':
					SValue = value;
					seenSCommand = true;
					break;
				case '*':
					checksum = value;
					seenStarCommand = true;
					break;
				default:
					resendCommand("Incorrect command",lineNumber);
			}
		}
		
		if(seenNCommand && seenStarCommand)
		{
			if(!verifyChecksum(cb.chars,cb.length,checksum))
			{
				resendCommand("Incorrect checksum",lineNumber);
				return;
			}
		}
		if(seenGCommand)
		{
			switch(commandNumber)
			{
				case 0:
				case 1:
					G1.enqueue(seenXCommand,seenYCommand,seenZCommand,seenECommand,seenFCommand,XValue,YValue,ZValue,EValue,FValue);
					break;
				default:
					resendCommand("Unknown G command",lineNumber);
			}
		}
	}
	
	private static void resendCommand(String message, int lineNumber)
	{
		System.out.print("rs ");
		if(lineNumber >= 0)
		{
			System.out.print(lineNumber);
		}
		System.out.print("//");
		System.out.print(message);
		
	}
	
	private static boolean verifyChecksum(char[] chars, int length, int checksum)
	{
		int calculatedChecksum = 0;
		for(int i = 0; i < length && chars[i] != '*'; i++)
		{
			calculatedChecksum = calculatedChecksum ^ chars[i];
		}
		return (calculatedChecksum == checksum);
	}
	
}
