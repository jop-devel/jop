package org.reprap;

public class G1 extends Command
{
	private static final int POOL_SIZE = 15;
	private static G1 first;
	private static G1 last;
	private static Object lock = new Object();
	private static boolean initialized = false;
	
	private G1 next;
	private boolean XSet = false;
	private int X = 0;
	private boolean YSet = false;
	private int Y = 0;
	private boolean ZSet = false;
	private int Z = 0;
	private boolean ESet = false;
	private int E = 0;
	private boolean FSet = false;
	private int F = 0;
	private boolean executed = false;
	
	//The G1 command is put into the Command queue, NOT the G1 pool
	public static boolean enqueue(boolean XSet, boolean YSet, boolean ZSet, boolean ESet, boolean FSet, int X, int Y, int Z, int E, int F)
	{
		if(!initialized)
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
		}
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
		temp.XSet = XSet;
		temp.YSet = YSet;
		temp.ZSet = ZSet;
		temp.ESet = ESet;
		temp.FSet = FSet;
		temp.X = X;
		temp.Y = Y;
		temp.Z = Z;
		temp.E = E;
		temp.F = F;
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
			instance.setTarget(XSet,YSet,ZSet,ESet,FSet,X,Y,Z,E,F);
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
