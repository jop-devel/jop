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

public class SerialController extends PeriodicEventHandler
{
	private static SerialController instance;
	
	public static SerialController getInstance()
	{
		if(instance == null)
		{
			instance = new SerialController();
		}
		return instance;
	}
	
	private SerialController()
	{
		super(new PriorityParameters(1),
			  new PeriodicParameters(null, new RelativeTime(1,1000)),
			  new StorageParameters(500, null, 0, 0));
	}
	
	
	LedSwitchFactory LSF = LedSwitchFactory.getLedSwitchFactory();
	LedSwitch LS = LSF.getLedSwitch();
	char[] chars = new char[32];
	// First int contains number and second contains the length of number in char[]
	int[] intParseResults = new int[2];
	boolean cmdRdy = false;
	boolean comment = false;
	CharacterBuffer buffer;
	boolean initialized = false;
	
	@Override
	public void handleAsyncEvent()
	{
		if(!initialized)
		{
			buffer = CharacterBuffer.getEmptyBuffer();
			System.out.println("start");
			initialized = true;
		}
		try
		{
			char character = '0';
			while(System.in.available() != 0)
			{
				character = (char)System.in.read();
				if(character == ';')
				{
					comment = true;
				}
				else if(character == '\n' || character == '\r')
				{
					comment = false;
					if(buffer.length > 0)
					{
						//buffer.returnToPool();
						//RepRapController.getInstance().LS.ledSwitch = 0xFFFFFFFF;
						//buffer = CharacterBuffer.getEmptyBuffer();
						//RepRapController.getInstance().LS.ledSwitch = 0xFFFFFFF0;
					}
				}
				else if(buffer.length < CharacterBuffer.BUFFER_WIDTH && !comment)
				{
					//Ignore too long command lines. Hopefully full of comments
					buffer.chars[buffer.length++] = character;
				}
			}
		}
		catch(Exception e)
		{
			System.out.print("ERROR:");
			System.out.print(e.getMessage());
		}
	}
}