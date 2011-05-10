package javax.safetycritical.test;

import javax.realtime.PriorityParameters;
import javax.realtime.RelativeTime;
import javax.safetycritical.Mission;
import javax.safetycritical.PeriodicParameters;
import javax.safetycritical.StorageParameters;

public class MissionImpl extends Mission{
	
	@Override
	protected void initialize() {
		StorageParameters sp = new StorageParameters(1000, 0, 0);
		new PeriodicEventHandlerImpl(new PriorityParameters(1), new PeriodicParameters(new RelativeTime(0, 0), new RelativeTime(2000, 0)),sp, "Periodic Event Handler 1\n");
		new PeriodicEventHandlerImpl(new PriorityParameters(2), new PeriodicParameters(new RelativeTime(0, 0), new RelativeTime(1000, 0)),sp, "Periodic Event Handler 2\n");
		new PeriodicEventHandlerImpl(new PriorityParameters(3), new PeriodicParameters(new RelativeTime(0, 0), new RelativeTime(500, 0)),sp, "Periodic Event Handler 3\n");
		new PeriodicEventHandlerImpl(new PriorityParameters(4), new PeriodicParameters(new RelativeTime(0, 0), new RelativeTime(3000, 0)),sp, "Periodic Event Handler 4\n");
	}
}
