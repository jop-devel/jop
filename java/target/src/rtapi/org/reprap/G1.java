package org.reprap;

public class G1 extends Command
{
	private static final int POOL_SIZE = 15;
	private static G1 first;
	private static G1 last;
	private static Object lock = new Object();
	private static boolean initialized = initialize(); //Ensures that pool is created in immortal memory so that all PEH have access
	
	private G1 next;
	private Parameter parameters = new Parameter();
	private boolean executed = false;
	
	private static boolean initialize()
	{
		//No need for mutex as the pool is empty
		G1 current = new G1();
		first = current;
		for(int i = 0; i < POOL_SIZE-1; i++)
		{
			G1 temp = new G1();
			current.next = temp;
			current = temp;
		}
		last = current;
		initialized = true;
		return true;
	}
	
	//The G1 command is put into the Command queue, NOT the G1 pool
	public static boolean enqueue(Parameter parameters)
	{
		G1 temp;
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
				synchronized (last) 
				{
					last = null;
				}
			}
		}
		temp.parameters.X = parameters.X;
		temp.parameters.Y = parameters.Y;
		temp.parameters.Z = parameters.Z;
		temp.parameters.E = parameters.E;
		temp.parameters.F = parameters.F;
		temp.parameters.S = parameters.S;
		temp.executed = false;
		Command.enqueue(temp);
		return true;
	}
	
	@Override
	public boolean execute() 
	{
		RepRapController instance = RepRapController.getInstance();
		//Execute
		if(!executed)
		{
			instance.setTarget(parameters);
			executed=true;
		}
		
		if(instance.inPosition())
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
}
