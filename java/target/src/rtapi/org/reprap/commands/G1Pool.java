package org.reprap.commands;

import org.reprap.CommandController;
import org.reprap.HostController;
import org.reprap.Parameter;
import org.reprap.RepRapController;

public class G1Pool 
{
	private final static int POOL_SIZE = 30;
	
	private G1 first;
	private G1 last;

	public G1Pool(HostController hostController, CommandController commandController, RepRapController repRapController)
	{
		//No need for mutex as the pool is empty
		G1 current = new G1(hostController,commandController,repRapController,this);
		first = current;
		for(int i = 0; i < POOL_SIZE-1; i++) //@WCA loop=30
		{
			G1 temp = new G1(hostController,commandController,repRapController,this);
			current.next = temp;
			current = temp;
		}
		last = current;
	}
	
	//The G1 command is put into the Command queue, NOT the G1 pool
	public boolean enqueue(Parameter parameters)
	{
		G1 temp = retreiveFromPool();
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
	
	synchronized private G1 retreiveFromPool()
	{
		G1 temp;
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
	
	synchronized public void returnToPool(G1 command)
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
