package javax.safetycritical.test;

import javax.safetycritical.PeriodicParameters;
import javax.realtime.PriorityParameters;
import javax.safetycritical.PeriodicEventHandler;
import javax.safetycritical.StorageParameters;

public class PeriodicEventHandlerImpl extends PeriodicEventHandler {
	
	String message;

	public PeriodicEventHandlerImpl(PriorityParameters priority,PeriodicParameters parameters,
            StorageParameters scp, String message) 
	{
		super(priority, parameters, scp);
		this.message = message;
	}

	@Override
	public void handleAsyncEvent() {
		System.out.print(message);
	}
}
