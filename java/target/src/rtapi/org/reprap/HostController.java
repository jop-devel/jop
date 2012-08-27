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

import com.jopdesign.io.IOFactory;
import com.jopdesign.io.SerialPort;

public class HostController extends PeriodicEventHandler
{
	public final static int MAX_STRING_LENGTH = 64;
	//From Integer
	private static final char[] digits = {
	    '0', '1', '2', '3', '4', '5', '6', '7', '8', '9',
	    'a', 'b', 'c', 'd', 'e', 'f', 'g', 'h', 'i', 'j',
	    'k', 'l', 'm', 'n', 'o', 'p', 'q', 'r', 's', 't',
	    'u', 'v', 'w', 'x', 'y', 'z',
	};
	
	private CharacterBuffer buffer = new CharacterBuffer();
	private boolean comment = false;
	private Object bufferLock = new Object();
	private int lineNumber = 0;
	private SerialPort SP = IOFactory.getFactory().getSerialPort();
	
	HostController()
	{
		super(new PriorityParameters(2),
			  new PeriodicParameters(null, new RelativeTime(1,0)),
			  new StorageParameters(50, null, 0, 0), 5);
		//System.out.print("start\n\r");
	}
	
	@Override
	public void handleAsyncEvent()
	{
		//Buffer is still full so do nothing
		if(buffer.isReady())
		{
			return;
		}
		for (int i = 0; i < 32; i++) //@WCA loop <= 32
		{
			char character;
			try
			{
				if(!SP.rxFull())
				{
					//No input
					//System.out.print("");
					return;
				}
				character = (char)SP.read();
			}
			catch(Exception e)
			{
				/*synchronized (sendLock) 
				{
					System.out.print("ERROR:");
					System.out.print(e.getMessage());
				}*/
				return;
			}
			if(character == ';')
			{
				comment = true;
			}
			else if(character == '\n' || character == '\r')
			{
				comment = false;
				if(buffer.getCount() > 0)
				{
					buffer.setReady(true);
					lineNumber++;
					return;
				}
			}
			else if(!comment) //Ignore comments
			{
				if(!buffer.addChar(character))
				{
					print("rs // Command too long\n\r");
				}
			}
		}
	}
	
	void resendCommand(String message)
	{
		print("rs //");
		print(message);
	}
	
	public void confirmCommand(String message)
	{
		print("ok //");
		print(message);
	}
	
	char[] getLine()
	{
		char[] chars = buffer.getChars();
		buffer.setReady(false);
		return chars;
	}
	
	//If the host isn't waiting for the M110 ok, there is no guarantee what the line number will be  
	synchronized public void setLineNumber(int lineNumber)
	{
		this.lineNumber = lineNumber;
	}
	
	synchronized void print(String string)
	{
		for(int i = 0; i < string.length() && i < MAX_STRING_LENGTH; i++)
		{
			SP.write(string.charAt(i));
		}
	}
	
	void print(int integer)
	{
		System.out.println(23);
		SP.write(integer);
	    
		//////////From Integer////////////
		int radix = 10;
	    // For negative numbers, print out the absolute value w/ a leading '-'.
	    // Use an array large enough for a binary number.
	    char[] buffer = new char[33];
	    int i = 33;
	    boolean isNeg = false;
	    if (integer < 0)
		{
	    	isNeg = true;
	    	integer = -integer;
		
		    // When the value is MIN_VALUE, it overflows when made positive
		    if (integer < 0)
		    {
		    	buffer[--i] = digits[(int) (-(integer + radix) % radix)];
		    	integer = -(integer / radix);
		    }
		}
	    
	    do
	    {
	    	buffer[--i] = digits[integer % radix];
	    	integer /= radix;
	    }
	    while (integer > 0); //@WCA loop=33

	    if (isNeg)
	      buffer[--i] = '-';
	    
	    print(buffer);
	}
	
	synchronized void print(char[] chars)
	{
		for(int i = 0; i < chars.length && i < MAX_STRING_LENGTH; i++)
		{
			SP.write(chars[i]);
		}
	}
	
}
