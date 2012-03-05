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
import javax.safetycritical.PeriodicEventHandler;
import javax.safetycritical.StorageParameters;

public class CommandController extends PeriodicEventHandler
{
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
			  new PeriodicParameters(null, new RelativeTime(10,0)),
			  new StorageParameters(50, null, 0, 0), 5);
	}
	
	private int lineNumber = 0;
	private Parameter parameters = new Parameter();
	
	@Override
	public void handleAsyncEvent()
	{
		CharacterBuffer cb = CharacterBuffer.getReadyBuffer();
		if(cb == null)
		{
			//No commands to process
			return;
		}
		/*for (int i = 0; i < cb.length; i++) 
		{
			System.out.print(cb.chars[i]);
			if(i == cb.length-1)
			{
				cb.returnToPool();
				return;
			}
		}*/
		int index = 0;
		
		boolean seenNCommand = false;
		
		boolean seenGCommand = false;
		boolean seenMCommand = false;
		int commandNumber = Integer.MIN_VALUE;
		
		parameters.X = Integer.MIN_VALUE;
		parameters.Y = Integer.MIN_VALUE;
		parameters.Z = Integer.MIN_VALUE;
		parameters.E = Integer.MIN_VALUE;
		parameters.F = Integer.MIN_VALUE;
		parameters.S = Integer.MIN_VALUE;
		
		boolean seenStarCommand = false;
		int checksum = 0;
		
		
		while(index < cb.length)
		{
			char character = cb.chars[index];
			char command = character;
			index++;
			int numberLength = 0;
			int value = 0;
			boolean decimalpoint = false;
			int decimals = 0;
			while(index < cb.length)
			{
				character = cb.chars[index];
				if(Character.digit(character, 10) > -1)
				{
					value = value * 10 + character-48;//Numbers start at character position 48
					numberLength++;
					if(decimalpoint)
					{
						//Ignore the rest of the decimals
						if(decimals < RepRapController.DECIMALS)
						{
							decimals++;
						}
					}
				}
				else if(character == '.')
				{
					decimalpoint = true;
				}
				else
				{
					//Command delimiter
					break;
				}
				index++;
			}
			if(numberLength == 0)
			{
				//All commands should have a number
				continue;
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
				case 'F':
					parameters.F = value;
					break;
				case 'S':
					parameters.S = value;
					break;
				case 'X':
					for (int i = 0; i < RepRapController.DECIMALS-decimals; i++) 
					{
						value = value*10;
					}
					parameters.X = value;
					break;
				case 'Y':
					for (int i = 0; i < RepRapController.DECIMALS-decimals; i++) 
					{
						value = value*10;
					}
					parameters.Y = value;
					break;
				case 'Z':
					for (int i = 0; i < RepRapController.DECIMALS-decimals; i++) 
					{
						value = value*10;
					}
					parameters.Z = value;
					break;
				case 'E':
					for (int i = 0; i < RepRapController.DECIMALS-decimals; i++) 
					{
						value = value*10;
					}
					parameters.E = value;
					break;
				case '*':
					checksum = value;
					seenStarCommand = true;
					break;
				default:
			}
		}
		if(seenNCommand && seenStarCommand)
		{
			if(!verifyChecksum(cb.chars,cb.length,checksum))
			{
				resendCommand("Incorrect checksum",lineNumber);
				cb.returnToPool();
				return;
			}
		}
		else if(seenGCommand)
		{
			switch(commandNumber)
			{
				case 0:
				case 1:
					//Send ok to host here
					G1.enqueue(parameters);
					break;
				default:
					resendCommand("Unknown G command",lineNumber);
					cb.returnToPool();
					return;
			}
		}
		else
		{
			resendCommand("Unknown command!",lineNumber);
			cb.returnToPool();
			return;
		}
		cb.returnToPool();
		lineNumber++;
	}
	
	private static void resendCommand(String message, int lineNumber)
	{
		if(lineNumber >= 0)
		{
			System.out.print(lineNumber);
		}
		System.out.print("//");
		System.out.print(message);
		System.out.print("\n");
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
