package test.level0;

import javax.realtime.PeriodicParameters;
import javax.realtime.PriorityParameters;
import javax.safetycritical.PeriodicEventHandler;
import javax.safetycritical.StorageParameters;

public class PEH extends PeriodicEventHandler{

	public PEH(PriorityParameters priority, PeriodicParameters parameters,
			StorageParameters scp, long scopeSize) {
		super(priority, parameters, scp, scopeSize);
		// TODO Auto-generated constructor stub
	}

	@Override
	public void handleAsyncEvent() {
		// TODO Auto-generated method stub
		
	}

}
