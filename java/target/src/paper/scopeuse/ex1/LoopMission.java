package scopeuse.ex1;

import javax.realtime.PeriodicParameters;
import javax.realtime.PriorityParameters;
import javax.realtime.RelativeTime;
import javax.safetycritical.Mission;
import javax.safetycritical.StorageParameters;

import scopeuse.ex4.ScMethodHandler;

/**
 * 
 * @author jrri
 *
 */

public class LoopMission extends Mission{
	
	LoopMission single = this;

	@Override
	protected void initialize() {
		
		PriorityParameters tempPrio = new PriorityParameters(13);
		
		RelativeTime tStart = new RelativeTime(0,0);
		RelativeTime tPeriod = new RelativeTime(1000, 0);
		PeriodicParameters tempPeriod = new PeriodicParameters(tStart, tPeriod);
		
		StorageParameters tempStorage = new StorageParameters(1024, null, 0, 0);
				
		ScMethodHandler t = new ScMethodHandler(tempPrio, tempPeriod, tempStorage, 512);
		t.register();
	}

	@Override
	public long missionMemorySize() {
		// TODO Auto-generated method stub
		return 0;
	}

}
