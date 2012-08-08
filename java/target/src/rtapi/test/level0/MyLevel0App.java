package test.level0;

import javax.realtime.PeriodicParameters;
import javax.realtime.PriorityParameters;
import javax.realtime.RelativeTime;
import javax.safetycritical.CyclicExecutive;
import javax.safetycritical.CyclicSchedule;
import javax.safetycritical.MissionSequencer;
import javax.safetycritical.Safelet;
import javax.safetycritical.StorageParameters;

public class MyLevel0App extends CyclicExecutive implements Safelet{

	public MyLevel0App(MissionSequencer<CyclicExecutive> sequencer) {
		super(sequencer);
	}

	// Safelet methods
	@Override
	public MissionSequencer getSequencer() {
		return null;
	}

	@Override
	public long immortalMemorySize() {
		return 0;
	}

	// Mission methods
	@Override
	protected void initialize() {
		
		PriorityParameters prio = new PriorityParameters(10);
		
		RelativeTime start = new RelativeTime();
		RelativeTime period = new RelativeTime(1000,0);
		
		PeriodicParameters pp = new PeriodicParameters(start, period);
		
		StorageParameters scp = new StorageParameters(1024, null);
		
		PEH HandlerA = new PEH(prio, pp, scp, 512);
		HandlerA.register();
		
	}

	@Override
	public long missionMemorySize() {
		return 0;
	}
	
	

}
