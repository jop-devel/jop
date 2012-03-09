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
	private static final int MAX_NUMBER_LENGTH = 8;
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
			  new StorageParameters(50, null, 0, 0), 5);
	}
	
	private int lineNumber = 0;
	private Parameter parameters = new Parameter();
	private char[] buffer = new char[64];
	private int chars = 0;
	private boolean ready = false;
	private boolean comment = false;
	private boolean initialized = false;
	private int timeout = -1;
	private Object lock = new Object();
	
	//If the host isn't waiting for the M110 ok, there is no guarantee what the line number will be  
	public void setLineNumber(int lineNumber)
	{
		this.lineNumber = lineNumber;
	}
	
	public void resendCommand(String message)
	{
		synchronized (lock) 
		{
			System.out.print("rs ");
			if(lineNumber >= 0)
			{
				System.out.print(lineNumber);
			}
			System.out.print(" //");
			System.out.print(message);
			System.out.print("\n\r");
		}
	}
	
	public void confirmCommand(String message)
	{
		synchronized (lock) 
		{
			System.out.print("ok ");
			System.out.print("//");
			if(lineNumber >= 0)
			{
				System.out.print(lineNumber);
			}
			if(message != null)
			{
				System.out.print(message);
			}
			System.out.print("\n\r");
		}
	}
	
	private boolean readSerial()
	{
		if(ready)
		{
			chars = 0;
			ready = false;
			comment = false;
		}
		for (int i = chars; i < buffer.length; i++) 
		{
			char character;
			try
			{
				if(System.in.available() == 0)
				{
					//No input
					System.out.print("");
					return false;
				}
				character = (char)System.in.read();
			}
			catch(Exception e)
			{
				System.out.print("ERROR:");
				System.out.print(e.getMessage());
				return false;
			}
			if(character == ';')
			{
				comment = true;
			}
			else if(character == '\n' || character == '\r')
			{
				comment = false;
				if(chars > 0)
				{
					ready = true;
					return true;
				}
			}
			else if(chars < buffer.length && !comment)
			{
				//Ignore too long command lines. Hopefully full of comments
				buffer[chars++] = character;
			}
		}
		chars = 0;
		ready = false;
		comment = false;
		resendCommand("command too long");
		return false;
	}
	
	@Override
	public void handleAsyncEvent()
	{
		if(!initialized)
		{
			initialized = true;
			System.out.print("start\n\r");
		}
		if(timeout == 500)
		{
			timeout = -1;
			resendCommand("Command buffer full");
		}
		else if(timeout >= 0)
		{
			timeout++;
			return;
		}
		if(!readSerial())
		{
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
				resendCommand("Incorrect checksum");
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
					if(!G1.enqueue(parameters))
					{
						timeout = 0;
						return;
					}
					confirmCommand(null);
					break;
				case 21:
					G21.enqueue();
					break;
				case 28:
					//Buffered command
					if(!G28.enqueue(parameters))
					{
						timeout = 0;
						return;
					}
					confirmCommand(null);
					break;
				case 90:
					G90.enqueue();
					break;
				case 92:
					G92.enqueue(parameters);
					break;
				default:
					resendCommand("Unknown4 G command");
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
					M110.enqueue(lineNumber);
					break;
				case 113:
					M113.enqueue();
					break;
				case 140:
					M140.enqueue();
					break;
				default:
					resendCommand("Unknown3 M command");
					return;
			}
		}
		else if(seenTCommand)
		{
			T.enqueue(commandNumber);
		}
		else
		{
			resendCommand("Unknown2 command!");
			return;
		}
		lineNumber++;
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
