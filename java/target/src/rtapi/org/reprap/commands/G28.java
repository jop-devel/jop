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

import org.reprap.Command;
import org.reprap.Parameter;
import org.reprap.RepRapController;

public class G28 extends Command
{
	private static final int POOL_SIZE = 15;
	private static G28 first;
	private static G28 last;
	private static Object lock = new Object();
	private static boolean initialized = initialize(); //Ensures that pool is created in immortal memory so that all PEH have access
	
	private G28 next;
	private Parameter parameters = new Parameter();
	private boolean executed = false;
	private RepRapController repRapController;
	
	private static boolean initialize()
	{
		//No need for mutex as the pool is empty
		G28 current = new G28();
		first = current;
		for(int i = 0; i < POOL_SIZE-1; i++)
		{
			G28 temp = new G28();
			current.next = temp;
			current = temp;
		}
		last = current;
		initialized = true;
		return true;
	}
	
	//The G28 command is put into the Command queue, NOT the G28 pool
	public static boolean enqueue(Parameter parameters, RepRapController repRapController)
	{
		G28 temp;
		synchronized (lock) 
		{
			if(first == null)
			{
				//Empty pool
				return false;
			}
			temp = first;
			first = temp.next;
			if(first == null)
			{
				last = null;
			}
		}
		//Try to reach endstops by moving towards them 1 meter
		if(parameters.X != Integer.MIN_VALUE)
		{
			temp.parameters.X = 0;//-1000*RepRapController.DECIMALS*10;
		}
		if(parameters.Y != Integer.MIN_VALUE)
		{
			temp.parameters.Y = 0;//-1000*RepRapController.DECIMALS*10;
		}
		if(parameters.Z != Integer.MIN_VALUE)
		{
			temp.parameters.Z = 0;//-1000*RepRapController.DECIMALS*10;
		}
		temp.executed = false;
		temp.repRapController = repRapController;
		Command.enqueue(temp);
		return true;
	}
	
	@Override
	public boolean execute() 
	{
		if(!executed)
		{
			repRapController.setTarget(parameters);
			executed=true;
		}
		if(repRapController.inPosition())
		{
			returnToPool();
			return true;
		}
		return false;
	}
	
	private void returnToPool()
	{
		synchronized (lock) 
		{
			if(last == null)
			{
				//Empty queue
				first = this;
			}
			else
			{
				last.next = this;
			}
			last = this;
			next = null;
		}
	}
	
	@Override
	public void respond() 
	{
		//Do nothing, already responded to this buffered command
	}
}
