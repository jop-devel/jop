package csp.scj;

import javax.realtime.PeriodicParameters;
import javax.realtime.PriorityParameters;
import javax.realtime.RelativeTime;
import javax.safetycritical.Mission;
import javax.safetycritical.StorageParameters;

public class WatchDogMission extends Mission{

	int nextSlave = 0;
	
	@Override
	protected void initialize() {
		

		// Watchdog handler. All parameters created explicitly
		RelativeTime start = new RelativeTime(0,0);
		RelativeTime period = new RelativeTime(2, 0);

		PriorityParameters wdPrio = new PriorityParameters(11);
		PeriodicParameters wdPeriod = new PeriodicParameters(start, period);
		StorageParameters  wdStorage = new StorageParameters(512, new long[] {256},0,0);

		WatchdogHandler WD = new WatchdogHandler(wdPrio, wdPeriod, wdStorage, 0, nextSlave);
		WD.register();

		// Polling task handler. All parameters created explicitly
		RelativeTime pTaskStart = new RelativeTime(1,0);
		RelativeTime pTaskPeriod = new RelativeTime(2, 0);

		PriorityParameters pTaskPrio = new PriorityParameters(12);
		PeriodicParameters pTaskPeriodic = new PeriodicParameters(pTaskStart, pTaskPeriod);
		StorageParameters  pTaskStorage = new StorageParameters(512, new long[] {256},0,0);

		PollingTask PT = new PollingTask(pTaskPrio, pTaskPeriodic, pTaskStorage, 0);
		PT.register();

		
	}

	@Override
	public long missionMemorySize() {
		// TODO Auto-generated method stub
		return 0;
	}

}
