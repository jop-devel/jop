package scopeuse.ex2;

import javax.realtime.PeriodicParameters;
import javax.realtime.PriorityParameters;
import javax.realtime.RelativeTime;
import javax.safetycritical.Mission;
import javax.safetycritical.StorageParameters;

public class ExeWithRetMission extends Mission{
	
	ExeWithRetMission single = this;

	@Override
	protected void initialize() {
		
		PriorityParameters tempPrio = new PriorityParameters(13);
		
		RelativeTime tStart = new RelativeTime(0,0);
		RelativeTime tPeriod = new RelativeTime(1000, 0);
		PeriodicParameters tempPeriod = new PeriodicParameters(tStart, tPeriod);
		
		StorageParameters tempStorage = new StorageParameters(1024, null, 0, 0);
				
		ExecWithRetHandler t = new ExecWithRetHandler(tempPrio, tempPeriod, tempStorage, 512);
		t.register();
	}

	@Override
	public long missionMemorySize() {
		// TODO Auto-generated method stub
		return 0;
	}

}
