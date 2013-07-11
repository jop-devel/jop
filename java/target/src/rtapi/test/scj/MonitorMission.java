package test.scj;

import javax.realtime.PeriodicParameters;
import javax.realtime.PriorityParameters;
import javax.realtime.RelativeTime;
import javax.safetycritical.Mission;
import javax.safetycritical.StorageParameters;

public class MonitorMission extends Mission{
	
	static final int TEMP_PRIO = 10;
	static final int VOLT_PRIO = 11;

	@Override
	protected void initialize() {
		// TODO Auto-generated method stub

		// Temperature handler. All parameters created explicitly
		PriorityParameters tempPrio = new PriorityParameters(TEMP_PRIO);
		
		RelativeTime tStart = new RelativeTime(0,0);
		RelativeTime tPeriod = new RelativeTime(1000, 0);
		PeriodicParameters tempPeriod = new PeriodicParameters(tStart, tPeriod);
		
		StorageParameters tempStorage = new StorageParameters(1000, null);
				
		Temperature t = new Temperature(tempPrio, tempPeriod, tempStorage, 500);
		t.register();
		
		// Voltage handler. All parameters created explicitly
		PriorityParameters voltPrio = new PriorityParameters(VOLT_PRIO);
		
		RelativeTime vStart = new RelativeTime(2000,0);
		RelativeTime vPeriod = new RelativeTime(1000, 0);
		PeriodicParameters voltPeriod = new PeriodicParameters(vStart, vPeriod);
		
		StorageParameters voltStorage = new StorageParameters(1000, null);
				
		Voltage v = new Voltage(voltPrio, voltPeriod, voltStorage, 500);
		
		v.register();
		
	}

	@Override
	public long missionMemorySize() {
		// TODO Auto-generated method stub
		return 400;
	}

}
