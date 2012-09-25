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


public class CommandController extends PeriodicEventHandler
{
	private Command first;
	private Command last;
	
	CommandController()
	{
		super(new PriorityParameters(3),
				new PeriodicParameters(null, new RelativeTime(20,0)),
				new StorageParameters(100, new long[]{100}, 0, 0), 0);
	}
	
	@Override
	public void handleAsyncEvent()
	{
		Command	temp;
		temp = getFirst();
		if(temp != null)
		{
			if(temp.execute())
			{
				temp.respond();
				setFirst(temp.next);
				temp.next = null;
			}
		}
	}
	
	synchronized private Command getFirst()
	{
		return first;
	}
	
	synchronized private void setFirst(Command command)
	{
		first = command;
		if(first == null)
		{
			last = null;
		}
	}
	
	synchronized public void enqueue(Command command)
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
