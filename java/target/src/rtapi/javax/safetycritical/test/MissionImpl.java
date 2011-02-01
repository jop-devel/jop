package javax.safetycritical.test;

import javax.safetycritical.Mission;

public class MissionImpl extends Mission{
	
	@Override
	protected void initialize() {
		new PeriodicEventHandlerImpl(1, 2000000, "Periodic Event Handler 1\n");
		new PeriodicEventHandlerImpl(2, 2500000, "Periodic Event Handler 2\n");
		new PeriodicEventHandlerImpl(3, 2000000, "Periodic Event Handler 3\n");
		new PeriodicEventHandlerImpl(4, 2500000, "Periodic Event Handler 4\n");
	}
}
