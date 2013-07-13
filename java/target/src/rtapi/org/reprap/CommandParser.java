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
	private static final int MAX_NUMBER_LENGTH = 5;
	
	private boolean waiting = false;
	private HostController hostController;
	private int lineNumber = 0;
	private char[] chars = new char[64];
	private int length = 0;
	
	private G1Pool G1Pool;
	private G28Pool G28Pool;
	private G21 G21;
	private G90 G90;
	private G91 G91;
	private G92 G92;
	private M82 M82;
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
//			  new StorageParameters(100, new long[]{100}, 0, 0), 0);
			  new StorageParameters(100, null, 0, 0), 100);
		this.hostController = hostController;
		G1Pool = new G1Pool(hostController, commandController, repRapController);
		G28Pool = new G28Pool(hostController, commandController, repRapController);
		G21 = new G21(hostController, commandController);
		G90 = new G90(hostController, commandController, repRapController);
		G91 = new G91(hostController, commandController, repRapController);
		G92 = new G92(hostController, commandController, repRapController);
		M82 = new M82(hostController, commandController);
		M104 = new M104(hostController, commandController, repRapController);
		M105 = new M105(hostController, commandController, repRapController);
		M109 = new M109(hostController, commandController, repRapController);
		M110 = new M110(hostController, commandController);
		M113 = new M113(hostController, commandController);
		M140 = new M140(hostController, commandController);
		T = new T(hostController, commandController);
		this.thread.setProcessor(3);
	}
	
	@Override
	public void handleAsyncEvent()
	{
		if(!waiting)
		{
			length = hostController.getLine(chars);
			if(length == 0)
			{
				return;
			}
		}
		else
		{
			waiting = false;
		}
		
		boolean seenNCommand = false;
		int lineNumberN = Integer.MIN_VALUE;
		
		boolean seenGCommand = false;
		boolean seenMCommand = false;
		boolean seenTCommand = false;
		int commandNumber = Integer.MIN_VALUE;
		
		Parameter parameter = new Parameter();
		
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
					if(numberLength < MAX_NUMBER_LENGTH && (!decimalpoint || decimals < RepRapController.NR_DECIMALS))
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
			
			int shiftedValue = value;
			for (int j = 0; j < RepRapController.NR_DECIMALS-decimals; j++) //@WCA loop = 1
			{
				shiftedValue = shiftedValue*10;
			}
			
			//Not using switch because of WCET analysis and JOP
			if(command == 'N')
			{
				lineNumberN = value;
				seenNCommand = true;
			}
			else if(command == 'G')
			{
				commandNumber = value;
				seenGCommand = true;
			}
			else if(command == 'M')
			{
				commandNumber = value;
				seenMCommand = true;
			}
			else if(command == 'F')
			{
				parameter.F = shiftedValue;
			}
			else if(command == 'S')
			{
				parameter.S = shiftedValue;
			}
			else if(command == 'T')
			{
				commandNumber = value;
				seenTCommand = true;
			}
			else if(command == 'X')
			{
				parameter.X = shiftedValue;
			}
			else if(command == 'Y')
			{
				parameter.Y = shiftedValue;
			}
			else if(command == 'Z')
			{
				parameter.Z = shiftedValue;
			}
			else if(command == 'E')
			{
				parameter.E = shiftedValue;
			}
			else if(command == '*')
			{
				checksum = value;
				seenStarCommand = true;
			}
		}
		
		if(seenNCommand && seenStarCommand)
		{
			if(!verifyChecksum(chars,checksum) || (lineNumberN != -1 && lineNumberN != lineNumber))
			{
				hostController.resendCommand(lineNumber,chars);
				return;
			}
		}
		
		if(seenGCommand)
		{
			if(commandNumber == 0 || commandNumber == 1)
			{
				//Buffered command
				if(!G1Pool.enqueue(parameter))
				{
					waiting = true;
					return;
				}
				hostController.confirmCommand(null);
			}
			else if(commandNumber == 21)
			{
				waiting = !G21.enqueue();
			}
			else if(commandNumber == 28)
			{
				//Buffered command
				if(!G28Pool.enqueue(parameter))
				{
					waiting = true;
					return;
				}
				hostController.confirmCommand(null);
			}
			else if(commandNumber == 90)
			{
				waiting = !G90.enqueue();
			}
			else if(commandNumber == 91)
			{
				waiting = !G91.enqueue();
			}
			else if(commandNumber == 92)
			{
				waiting = !G92.enqueue(parameter);
			}
			else
			{
				hostController.resendCommand(lineNumberN,chars);
				return;
			}
		}
		else if(seenMCommand)
		{
			if(commandNumber == 82)
			{
				waiting = !M82.enqueue();
			}
			else if(commandNumber == 104)
			{
				waiting = !M104.enqueue(parameter.S);
			}
			else if(commandNumber == 105)
			{
				waiting = !M105.enqueue();
			}
			else if(commandNumber == 109)
			{
				waiting = !M109.enqueue(parameter.S);
			}
			else if(commandNumber == 110)
			{
				if(!M110.enqueue())
				{
					waiting = true;
					return;
				}
				lineNumber = lineNumberN;
			}
			else if(commandNumber == 113)
			{
				waiting = !M113.enqueue();
			}
			else if(commandNumber == 140)
			{
				waiting = !M140.enqueue();
			}
			else
			{
				hostController.resendCommand(lineNumberN,chars);
				return;
			}
		}
		else if(seenTCommand)
		{
			waiting = !T.enqueue();
		}
		else
		{
			hostController.resendCommand(lineNumberN,chars);
			return;
		}
		if(waiting)
		{
			return;
		}
		if(seenNCommand && seenStarCommand)
		{
			lineNumber++;
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
