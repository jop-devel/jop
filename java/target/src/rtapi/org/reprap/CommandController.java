package org.reprap;

import javax.realtime.PeriodicParameters;
import javax.realtime.PriorityParameters;
import javax.realtime.RelativeTime;
import javax.safetycritical.PeriodicEventHandler;
import javax.safetycritical.StorageParameters;


public class CommandController extends PeriodicEventHandler
{
	
	static CommandController instance;
	
	private Command first;
	private Command last;
	private Object lock = new Object();
	
	
	CommandController()
	{
		super(new PriorityParameters(3),
				new PeriodicParameters(null, new RelativeTime(10,0)),
				new StorageParameters(10, null, 0, 0), 5);
	}
	
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
	
	public void enqueue(Command command)
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

}
