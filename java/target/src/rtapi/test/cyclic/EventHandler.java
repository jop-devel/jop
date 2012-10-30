package test.cyclic;

import javax.realtime.PeriodicParameters;
import javax.realtime.PriorityParameters;
import javax.safetycritical.PeriodicEventHandler;
import javax.safetycritical.StorageParameters;

public class EventHandler extends PeriodicEventHandler{
	
	

public EventHandler(PriorityParameters priority,
			PeriodicParameters release, StorageParameters scp, long scopeSize,
			String name) {
		super(priority, release, scp, scopeSize, name);
	}

	@Override
	public void handleAsyncEvent() {
		
	}

}
