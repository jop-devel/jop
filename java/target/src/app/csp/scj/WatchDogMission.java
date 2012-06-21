package csp.scj;

import javax.realtime.PeriodicParameters;
import javax.realtime.PriorityParameters;
import javax.realtime.RelativeTime;
import javax.safetycritical.Mission;
import javax.safetycritical.StorageParameters;

public class WatchDogMission extends Mission{

	@Override
	protected void initialize() {

		// Watchdog handler. All parameters created explicitly
		RelativeTime start = new RelativeTime(0,0);
		RelativeTime period = new RelativeTime(20, 0);

		PriorityParameters wdPrio = new PriorityParameters(11);
		PeriodicParameters wdPeriod = new PeriodicParameters(start, period);
		StorageParameters  wdStorage = new StorageParameters(2048, null);

		WatchdogHandler WD = new WatchdogHandler(wdPrio, wdPeriod, wdStorage, 2048);
		WD.register();

	}

	@Override
	public long missionMemorySize() {
		// TODO Auto-generated method stub
		return 0;
	}

}
