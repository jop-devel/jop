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
package org.reprap.commands;

import org.reprap.CommandController;
import org.reprap.HostController;
import org.reprap.Parameter;
import org.reprap.RepRapController;

public class G28Pool 
{
	private final static int POOL_SIZE = 30;
	
	private G28 first;
	private G28 last;

	public G28Pool(HostController hostController, CommandController commandController, RepRapController repRapController)
	{
		//No need for mutex as the pool is empty
		G28 current = new G28(hostController,commandController,repRapController,this);
		first = current;
		for(int i = 0; i < POOL_SIZE-1; i++) //@WCA loop=30
		{
			G28 temp = new G28(hostController,commandController,repRapController,this);
			current.next = temp;
			current = temp;
		}
		last = current;
	}
	
	//The G28 command is put into the Command queue, NOT the G28 pool
	public boolean enqueue(Parameter parameters)
	{
		G28 temp = retreiveFromPool();
		if(temp == null)
		{
			return false;
		}
		temp.setParameters(parameters);
		if(!temp.enqueue())
		{
			returnToPool(temp);
			return false;
		}
		return true;
	}
	
	synchronized private G28 retreiveFromPool()
	{
		G28 temp;
		if(first == null)
		{
			//Empty pool
			return null;
		}
		temp = first;
		first = temp.next;
		if(first == null)
		{
			last = null;
		}
		return temp;
	}
	
	synchronized public void returnToPool(G28 command)
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
