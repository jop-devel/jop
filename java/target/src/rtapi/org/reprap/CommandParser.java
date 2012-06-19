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

import javax.realtime.PeriodicParameters;
import javax.realtime.PriorityParameters;
import javax.realtime.RelativeTime;
import javax.safetycritical.PeriodicEventHandler;
import javax.safetycritical.StorageParameters;

import org.reprap.commands.G1;
import org.reprap.commands.G21;
import org.reprap.commands.G28;
import org.reprap.commands.G90;
import org.reprap.commands.G92;
import org.reprap.commands.M105;
import org.reprap.commands.M109;
import org.reprap.commands.M110;
import org.reprap.commands.M113;
import org.reprap.commands.M140;
import org.reprap.commands.T;

public class CommandParser extends PeriodicEventHandler
{
	private static final int MAX_NUMBER_LENGTH = 8;
	private static CommandParser instance;
	
	static CommandParser getInstance(HostController hostController, RepRapController repRapController)
	{
		if(instance == null)
		{
			instance = new CommandParser(hostController,repRapController);
		}
		return instance;
	}
	
	private HostController hostController;
	private RepRapController repRapController;
	private Parameter parameters = new Parameter();
	
	private int chars = 0;
	private char[] buffer = new char[64];
	
	private boolean waitingG1Command = false;
	private boolean waitingG28Command = false;
	
	private CommandParser(HostController hostController, RepRapController repRapController)
	{
		super(new PriorityParameters(1),
			  new PeriodicParameters(null, new RelativeTime(1,0)),
			  new StorageParameters(50, null, 0, 0), 5);
		this.hostController = hostController;
		this.repRapController = repRapController;
	}
	
	@Override
	public void handleAsyncEvent()
	{
		if(waitingG1Command)
		{
			if(!G1.enqueue(parameters,repRapController))
			{
				//The command buffer is full so no need to parse further commands until there is space
				return;
			}
			waitingG1Command = false;
		}
		else if(waitingG28Command)
		{
			if(!G28.enqueue(parameters,repRapController))
			{
				//The command buffer is full so no need to parse further commands until there is space
				return;
			}
			waitingG28Command = false;
		}
		
		if(!HostController.getInstance().getLine(buffer))
		{
			return;
		}
		
		int index = 0;
		
		boolean seenNCommand = false;
		int lineNumber = 0;
		
		boolean seenGCommand = false;
		boolean seenMCommand = false;
		boolean seenTCommand = false;
		int commandNumber = Integer.MIN_VALUE;
		
		parameters.X = Integer.MIN_VALUE;
		parameters.Y = Integer.MIN_VALUE;
		parameters.Z = Integer.MIN_VALUE;
		parameters.E = Integer.MIN_VALUE;
		parameters.F = Integer.MIN_VALUE;
		parameters.S = Integer.MIN_VALUE;
		
		boolean seenStarCommand = false;
		int checksum = 0;
		
		while(index < chars)
		{
			char character = buffer[index];
			char command = character;
			index++;
			int numberLength = 0;
			int value = 0;
			boolean decimalpoint = false;
			int decimals = 0;
			while(index < chars)
			{
				character = buffer[index];
				if(Character.digit(character, 10) > -1)
				{
					//Ignore the rest of the decimals
					if(numberLength < MAX_NUMBER_LENGTH && (!decimalpoint || decimals < RepRapController.DECIMALS))
					{
						value = value * 10 + character-48;//Numbers start at character position 48
						numberLength++;
						if(decimalpoint)
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
				case 'T':
					commandNumber = value;
					seenTCommand = true;
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
			if(!verifyChecksum(buffer,chars,checksum))
			{
				hostController.resendCommand("Incorrect checksum");
				return;
			}
		}
		if(seenGCommand)
		{
			switch(commandNumber)
			{
				case 0://Same as G1
				case 1:
					//Buffered command
					if(!G1.enqueue(parameters,repRapController))
					{
						waitingG1Command = true;
						return;
					}
					hostController.confirmCommand(null);
					break;
				case 21:
					G21.enqueue();
					break;
				case 28:
					//Buffered command
					if(!G28.enqueue(parameters,repRapController))
					{
						waitingG28Command = true;
						return;
					}
					hostController.confirmCommand(null);
					break;
				case 90:
					G90.enqueue();
					break;
				case 92:
					G92.enqueue(parameters,repRapController);
					break;
				default:
					hostController.resendCommand("Unknown G command");
					return;
			}
		}
		else if(seenMCommand)
		{
			switch(commandNumber)
			{
				case 105:
					M105.enqueue();
					break;
				case 109:
					M109.enqueue();
					break;
				case 110:
					M110.enqueue(lineNumber,hostController);
					break;
				case 113:
					M113.enqueue();
					break;
				case 140:
					M140.enqueue();
					break;
				default:
					hostController.resendCommand("Unknown M command");
					return;
			}
		}
		else if(seenTCommand)
		{
			T.enqueue(commandNumber);
		}
		else
		{
			hostController.resendCommand("Unknown command!");
			return;
		}
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
