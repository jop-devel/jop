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
