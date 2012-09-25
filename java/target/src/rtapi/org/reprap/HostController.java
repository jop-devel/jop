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

import com.jopdesign.io.*;

public class HostController extends PeriodicEventHandler
{
	public final static int MAX_STRING_LENGTH = 64;
	
	private final static char[] OK = {'o','k',' '};
	private final static char[] RS = {'r','s',' '};
	private final static char[] DEBUG = {' ','/','/'};
	private final static char[] NEWLINE = {'\n'};
	private final static char[] COMMAND_TOO_LONG = {'C','o','m','m','a','n','d',' ','t','o','o',' ','l','o','n','g','!'};
	
	private CharacterBuffer inputBuffer = new CharacterBuffer(MAX_STRING_LENGTH);
	private int inputCount = 0;
	private boolean inputStatus = false;
	private CharacterBuffer outputBuffer = new CharacterBuffer(MAX_STRING_LENGTH);
	private boolean comment = false;
	
	private SerialPort host = IOFactory.getFactory().getSerialPort();
	//private HostSimulator host = new HostSimulator();
	
	HostController()
	{
		super(new PriorityParameters(2),
			  new PeriodicParameters(null, new RelativeTime(1,0)),
			  new StorageParameters(35, new long[]{35},0,0), 0);
	}
	
	synchronized private void setInputStatus(boolean status)
	{
		inputStatus = status;
	}
	
	synchronized private boolean getInputStatus()
	{
		return inputStatus;
	}
	
	@Override
	public void handleAsyncEvent()
	{
		char[] output = outputBuffer.getChars(16);
		for (int i = 0; i < output.length; i++) //@WCA loop = 16
		{
			host.write(output[i]);
		}
		//Input buffer is still full so do nothing
		if(getInputStatus())
		{
			return;
		}
		for (int i = 0; i < 16; i++) //@WCA loop = 16
		{
			char character;
			if(!host.rxFull())
			{
				//No input
				return;
			}
			character = (char)host.read();
			if(character == ';')
			{
				comment = true;
			}
			else if(character == '\n')
			{
				comment = false;
				if(inputCount > 0)
				{
					inputCount = 0;
					setInputStatus(true);
					return;
				}
			}
			else if(!comment) //Ignore comments
			{
				if(inputBuffer.add(character))
				{
					inputCount++;
				}
			}
		}
	}
	
	void resendCommand(int lineNumber, char[] debug)
	{
		if(lineNumber > Integer.MIN_VALUE)
		{
			outputBuffer.add(RS,intToChar(lineNumber),DEBUG,debug,NEWLINE);
		}
		else
		{
			outputBuffer.add(RS,DEBUG,debug,NEWLINE);
		}
		
	}
	
	void resendCommand(int lineNumber)
	{
		if(lineNumber > Integer.MIN_VALUE)
		{
			outputBuffer.add(RS,intToChar(lineNumber),NEWLINE);
		}
		else
		{
			outputBuffer.add(RS,NEWLINE);
		}
		
	}
	
	public void confirmCommand(char[] response)
	{
		outputBuffer.add(OK,response,NEWLINE);
	}
	
	public void confirmCommand(char[] response1, char[] response2, char[] response3)
	{
		outputBuffer.add(OK,response1,response2,response3,NEWLINE);
	}
	
	char[] getLine()
	{
		char[] chars = inputBuffer.getChars(0);
		//print(chars);
		setInputStatus(false);
		return chars;
	}
	
	private static final char[] digits = {'0', '1', '2', '3', '4', '5', '6', '7', '8', '9'};
		
	public static char[] intToChar(int integer)
	{
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
		    	buffer[--i] = digits[(int) (Math.modulo10(-(integer + radix)))];
		    	integer = -(Math.divs10(integer));
		    }
		}
	    
	    buffer[--i] = digits[Math.modulo10(integer)];
    	integer = Math.divs10(integer);
	    for(; integer > 0;) //@WCA loop = 33
	    {
	    	buffer[--i] = digits[Math.modulo10(integer)];
	    	integer = Math.divs10(integer);
	    }

	    if (isNeg)
	      buffer[--i] = '-';
	    
	    
	    char[] newBuffer = new char[33-i];
	    for (int j = 0; j < newBuffer.length; j++) //@WCA loop = 33
	    {
	    	newBuffer[j] = buffer[i++];
		}
	    return newBuffer;
	}
	
	private void print(char[] chars)
	{
		outputBuffer.add(chars);
	}
	
}
