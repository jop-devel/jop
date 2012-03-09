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

public abstract class Command 
{
	private static Command first;
	private static Command last;
	private static Object lock = new Object();
	
	
	private static PeriodicEventHandler worker;
	
	public static PeriodicEventHandler getInstance()
	{
		if(worker == null)
		{
			worker = new PeriodicEventHandler(new PriorityParameters(1),
					new PeriodicParameters(null, new RelativeTime(10,0)),
					new StorageParameters(10, null, 0, 0))
			{
				@Override
				public void handleAsyncEvent()
				{
					Command	temp;
					synchronized (lock) 
					{
						temp = first;
					}
					if(temp != null)
					{
						if(temp.execute())
						{
							temp.respond();
							synchronized (lock) 
							{
								first = temp.next;
								if(first == null)
								{
									last = null;
								}
							}
							temp.next = null;
						}
					}
				}
			};
		}
		return worker;
	}
	
	protected static void enqueue(Command command)
	{
		synchronized (lock) 
		{
			if(last == null)
			{
				//Empty queue
				first = command;
			}
			else
			{
				last.next = command;
			}
			last = command;
			command.next = null;
		}
	}
	
	private Command next;
	
	public abstract boolean execute();
	
	public void respond()
	{
		//responds with rs
		CommandController.getInstance().confirmCommand("");
	}
}
