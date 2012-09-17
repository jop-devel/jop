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

import org.reprap.commands.*;

public class CommandParser extends PeriodicEventHandler
{
	private static final int MAX_NUMBER_LENGTH = 8;
	
	private Parameter parameter = new Parameter();
	private boolean waitingG1Command = false;
	private boolean waitingG28Command = false;
	private HostController hostController;
	
	private G1Pool G1Pool;
	private G28Pool G28Pool;
	private G21 G21;
	private G90 G90;
	private G91 G91;
	private G92 G92;
	private M104 M104;
	private M105 M105;
	private M109 M109;
	private M110 M110;
	private M113 M113;
	private M140 M140;
	private T T;
	
	CommandParser(HostController hostController, CommandController commandController, RepRapController repRapController)
	{
		super(new PriorityParameters(4),
			  new PeriodicParameters(null, new RelativeTime(60,0)),
			  new StorageParameters(70, new long[]{70}, 0, 0), 0);
		this.hostController = hostController;
		G1Pool = new G1Pool(hostController, commandController, repRapController);
		G28Pool = new G28Pool(hostController, commandController, repRapController);
		G21 = new G21(hostController, commandController);
		G90 = new G90(hostController, commandController, repRapController);
		G91 = new G91(hostController, commandController, repRapController);
		G92 = new G92(hostController, commandController, repRapController);
		M104 = new M104(hostController, commandController, repRapController);
		M105 = new M105(hostController, commandController, repRapController);
		M109 = new M109(hostController, commandController, repRapController);
		M110 = new M110(hostController, commandController);
		M113 = new M113(hostController, commandController);
		M140 = new M140(hostController, commandController);
		T = new T(hostController, commandController);
	}
	
	@Override
	public void handleAsyncEvent()
	{
		if(waitingG1Command)
		{
			if(!G1Pool.enqueue(parameter.clone()))
			{
				//The command buffer is full so no need to parse further commands until there is space
				return;
			}
			hostController.confirmCommand(null);
			waitingG1Command = false;
		}
		else if(waitingG28Command)
		{
			if(!G28Pool.enqueue())
			{
				//The command buffer is full so no need to parse further commands until there is space
				return;
			}
			hostController.confirmCommand(null);
			waitingG28Command = false;
		}
		char[] chars = hostController.getLine();
		
		if(chars.length == 0)
		{
			return;
		}
		int length = chars.length;
		boolean seenNCommand = false;
		int lineNumberN = Integer.MIN_VALUE;
		
		boolean seenGCommand = false;
		boolean seenMCommand = false;
		boolean seenTCommand = false;
		int commandNumber = Integer.MIN_VALUE;
		
		parameter.X = Integer.MIN_VALUE;
		parameter.Y = Integer.MIN_VALUE;
		parameter.Z = Integer.MIN_VALUE;
		parameter.E = Integer.MIN_VALUE;
		parameter.F = Integer.MIN_VALUE;
		parameter.S = Integer.MIN_VALUE;
		
		boolean seenStarCommand = false;
		int checksum = 0;
		
		for(int i = 0; i < length; i++) //@WCA loop = 64
		{
			char character = chars[i];
			char command = character;
			int numberLength = 0;
			int value = 0;
			boolean decimalpoint = false;
			boolean negative = false;
			int decimals = 0;
			for(int j = i+1; j < length; j++) //@WCA loop = 1
			{
				character = chars[j];
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
				else if(character == '.' && numberLength > 0)
				{
					decimalpoint = true;
				}
				else if(character == '-' && numberLength == 0)
				{
					negative = true;
				}
				else
				{
					//Command delimiter
					break;
				}
				i++;
			}
			if(numberLength == 0)
			{
				//All commands should have a number
				//hostController.resendCommand(INCORRECT_COMMAND);
				continue;
			}
			if(negative)
			{
				value = -value;
			}
			
			switch(command)
			{
				case 'N':
					lineNumberN = value;
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
					parameter.F = value;
					break;
				case 'S':
					for (int j = 0; j < RepRapController.DECIMALS-decimals; j++) //@WCA loop = 1
					{
						value = value*10;
					}
					parameter.S = value;
					break;
				case 'T':
					commandNumber = value;
					seenTCommand = true;
					break;
				case 'X':
					for (int j = 0; j < RepRapController.DECIMALS-decimals; j++) //@WCA loop = 1
					{
						value = value*10;
					}
					parameter.X = value;
					break;
				case 'Y':
					for (int j = 0; j < RepRapController.DECIMALS-decimals; j++) //@WCA loop = 1
					{
						value = value*10;
					}
					parameter.Y = value;
					break;
				case 'Z':
					for (int j = 0; j < RepRapController.DECIMALS-decimals; j++) //@WCA loop = 1
					{
						value = value*10;
					}
					parameter.Z = value;
					break;
				case 'E':
					for (int j = 0; j < RepRapController.DECIMALS-decimals; j++) //@WCA loop=1
					{
						value = value*10;
					}
					parameter.E = value;
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
			if(!verifyChecksum(chars,checksum))
			{
				hostController.resendCommand(lineNumberN,chars);
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
					if(!G1Pool.enqueue(parameter.clone()))
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
					if(!G28Pool.enqueue())
					{
						waitingG28Command = true;
						return;
					}
					hostController.confirmCommand(null);
					break;
				case 90:
					G90.enqueue();
					break;
				case 91:
					G91.enqueue();
					break;
				case 92:
					G92.enqueue(parameter.clone());
					break;
				default:
					hostController.resendCommand(lineNumberN,chars);
					return;
			}
		}
		else if(seenMCommand)
		{
			switch(commandNumber)
			{
				case 104:
					if(parameter.S != Integer.MIN_VALUE)
					{
						M104.enqueue(parameter.S);
					}
					break;
				case 105:
					M105.enqueue();
					break;
				case 109:
					if(parameter.S != Integer.MIN_VALUE)
					{
						M109.enqueue(parameter.S);
					}
					break;
				case 110:
					M110.enqueue(lineNumberN);
					break;
				case 113:
					M113.enqueue();
					break;
				case 140:
					M140.enqueue();
					break;
				default:
					hostController.resendCommand(lineNumberN,chars);
					return;
			}
		}
		else if(seenTCommand)
		{
			T.enqueue();
		}
		else
		{
			hostController.resendCommand(lineNumberN,chars);
			return;
		}
	}
	
	private static boolean verifyChecksum(char[] chars, int checksum)
	{
		int calculatedChecksum = 0;
		for(int i = 0; i < chars.length && chars[i] != '*'; i++) //@WCA loop = 64
		{
			calculatedChecksum = calculatedChecksum ^ chars[i];
		}
		return (calculatedChecksum == checksum);
	}
	
}
