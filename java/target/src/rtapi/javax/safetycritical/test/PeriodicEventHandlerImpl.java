package javax.safetycritical.test;

import javax.safetycritical.PeriodicParameters;
import javax.realtime.PriorityParameters;
import javax.safetycritical.PeriodicEventHandler;
import javax.safetycritical.StorageParameters;

public class PeriodicEventHandlerImpl extends PeriodicEventHandler {
	
	String message;
	int[] test = new int[100];
	int[] test2 = new int[100];

	public PeriodicEventHandlerImpl(PriorityParameters priority,PeriodicParameters parameters,
            StorageParameters scp, String message) 
	{
		super(priority, parameters, scp);
		this.message = message;
	}

	@Override
	public void handleAsyncEvent() {
		for (int i = 0; i < test.length; i++)
		{
			test[i] = 456;
		}
		System.arraycopy(test, 0, test2, 0, 100);
		boolean equal = true;
		for (int i = 0; i < test2.length; i++)
		{
			if(test2[i] != 456)
			{
				equal = false;
			}
		}
		if(equal)
		{
			System.out.print("All is ok:"+message);
			return;
		}
		System.out.print("Error:"+message);
	}
}
