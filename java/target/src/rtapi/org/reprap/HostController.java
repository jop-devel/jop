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
	public static HostController instance;
	
	private final String ok = "ok\n\r";
	private final String rs = "rs\n\r";
	
	private char[] buffer = new char[64];
	private boolean full = false;
	private boolean comment = false;
	private int characterCount = 0;
	private Object bufferLock = new Object();
	private int lineNumber = 0;
	
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
		synchronized (bufferLock) 
		{
			//Buffer is still full so do nothing
			if(full)
			{
				return;
			}
		}
		for (int i = 0; i < buffer.length; i++) //@WCA loop=64
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
				if(characterCount > 0)
				{
					synchronized (bufferLock) 
					{
						full = true;
						lineNumber++;
					}
					return;
				}
			}
			else if(!comment && characterCount < buffer.length) //Ignore comments
			{
				buffer[characterCount++] = character;
			}
		}
	}
	
	synchronized void resendCommand(String message)
	{
		//reply("rs",message);
		System.out.print(rs);
	}
	
	synchronized void confirmCommand(String message)
	{
		//reply("ok",message);
		System.out.print(ok);
	}
	
	private void reply(String type, String message)
	{
		/*int tempLineNumber;
		synchronized (bufferLock) 
		{
			tempLineNumber = lineNumber;
		}
		StringBuilder sb = new StringBuilder();
		sb.append(type);
		sb.append(" ");
		sb.append(tempLineNumber);
		sb.append("//");
		sb.append(message);
		sb.append("\n\r");
		synchronized (sendLock) 
		{
			System.out.print(sb);
		}*/
	}
	
	int getLine(char[] buffer)
	{
		synchronized (bufferLock) 
		{
			if(!full)
			{
				return 0;
			}
		}
		int tmpCharacterCount = characterCount;
		characterCount = 0;
		for (int i = 0; i < tmpCharacterCount; i++) //@WCA loop=64
		{
			buffer[i] = this.buffer[i];
		}
		synchronized (bufferLock) 
		{
			full = false;
			return tmpCharacterCount;
		}
	}
	
	//If the host isn't waiting for the M110 ok, there is no guarantee what the line number will be  
	public void setLineNumber(int lineNumber)
	{
		synchronized (bufferLock) 
		{
			this.lineNumber = lineNumber;
		}
	}
	
}
