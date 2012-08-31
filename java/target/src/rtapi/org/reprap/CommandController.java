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
				new StorageParameters(100, null, 0, 0), 5);
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
