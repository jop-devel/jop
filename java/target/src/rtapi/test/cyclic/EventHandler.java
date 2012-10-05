package test.cyclic;

import javax.realtime.PeriodicParameters;
import javax.realtime.PriorityParameters;
import javax.safetycritical.PeriodicEventHandler;
import javax.safetycritical.StorageParameters;

public class EventHandler extends PeriodicEventHandler{

	public EventHandler(PriorityParameters priority,
			PeriodicParameters parameters, StorageParameters scp, long scopeSize) {
		super(priority, parameters, scp, scopeSize);
	}

	@Override
	public void handleAsyncEvent() {
		
	}

}
