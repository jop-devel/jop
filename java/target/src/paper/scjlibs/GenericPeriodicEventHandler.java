package scjlibs;

import javax.realtime.PeriodicParameters;
import javax.realtime.PriorityParameters;
import javax.realtime.RelativeTime;
import javax.safetycritical.PeriodicEventHandler;
import javax.safetycritical.StorageParameters;

public abstract class GenericPeriodicEventHandler extends PeriodicEventHandler {

	public GenericPeriodicEventHandler(String name, int priority) {
		super(new PriorityParameters(priority),
				new PeriodicParameters(new RelativeTime(0, 0),
						new RelativeTime(Constants.PEH_PERIOD, 0)),
				new StorageParameters(Constants.BACKING_STORE, null),
				Constants.SCOPE_SIZE, name);
	}


}
