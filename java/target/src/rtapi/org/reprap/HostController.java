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

public class HostController extends PeriodicEventHandler
{
	private static HostController instance;
	
	static HostController getInstance()
	{
		if(instance == null)
		{
			instance = new HostController();
		}
		return instance;
	}
	
	private char[] buffer = new char[64];
	private boolean full = false;
	private boolean comment = false;
	private int characterCount = 0;
	private Object serialLock = new Object();
	private Object bufferLock = new Object();
	private int lineNumber = 0;
	
	private HostController()
	{
		super(new PriorityParameters(1),
			  new PeriodicParameters(null, new RelativeTime(1,0)),
			  new StorageParameters(50, null, 0, 0), 5);
		System.out.print("start\n\r");
	}
	
	@Override
	public void handleAsyncEvent()
	{
		synchronized (bufferLock) 
		{
			//Buffer is still full so do nothing
			if(full)
			{
				return;
			}
		}
		while (characterCount < buffer.length) 
		{
			char character;
			try
			{
				if(System.in.available() == 0)
				{
					//No input
					//System.out.print("");
					return;
				}
				character = (char)System.in.read();
			}
			catch(Exception e)
			{
				System.out.print("ERROR:");
				System.out.print(e.getMessage());
				return;
			}
			if(character == ';')
			{
				comment = true;
			}
			else if(character == '\n' || character == '\r')
			{
				comment = false;
				if(characterCount > 0)
				{
					synchronized (bufferLock) 
					{
						full = true;
					}
					synchronized (serialLock) 
					{
						lineNumber++;
					}
					return;
				}
			}
			else if(!comment) //Ignore comments
			{
				buffer[characterCount++] = character;
			}
		}
		characterCount = 0;
		resendCommand("command too long");
	}
	
	void resendCommand(String message)
	{
		synchronized (serialLock) 
		{
			System.out.print("rs ");
			System.out.print(lineNumber);
			System.out.print(" //");
			System.out.print(message);
			System.out.print("\n\r");
		}
	}
	
	void confirmCommand(String message)
	{
		synchronized (serialLock) 
		{
			System.out.print("ok ");
			System.out.print("//");
			System.out.print(lineNumber);
			if(message != null)
			{
				System.out.print(message);
			}
			System.out.print("\n\r");
		}
	}
	
	boolean getLine(char[] buffer)
	{
		synchronized (bufferLock) 
		{
			if(full)
			{
				for (int i = 0; i < buffer.length && i < characterCount; i++) 
				{
					buffer[i] = this.buffer[i];
				}
				full = false;
				return true;
			}
			return false;
		}
	}
	
	//If the host isn't waiting for the M110 ok, there is no guarantee what the line number will be  
	public void setLineNumber(int lineNumber)
	{
		synchronized (serialLock) 
		{
			this.lineNumber = lineNumber;
		}
	}
	
}
