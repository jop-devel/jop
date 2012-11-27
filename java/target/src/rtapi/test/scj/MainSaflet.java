package test.scj;

import javax.realtime.PriorityParameters;
import javax.safetycritical.LinearMissionSequencer;
import javax.safetycritical.Mission;
import javax.safetycritical.MissionSequencer;
import javax.safetycritical.Safelet;
import javax.safetycritical.StorageParameters;
import javax.safetycritical.annotate.Level;
import javax.safetycritical.annotate.Phase;
import javax.safetycritical.annotate.SCJAllowed;
import javax.safetycritical.annotate.SCJRestricted;

public class MainSaflet implements Safelet{

	@Override
	public MissionSequencer<Mission> getSequencer() {
		// TODO Auto-generated method stub
		
		StorageParameters sp = new StorageParameters(1000000000, null);
		MonitorMission m = new MonitorMission();
		return new LinearMissionSequencer<Mission>(new PriorityParameters(13), sp, m); 
	}

	@Override
	public long immortalMemorySize() {
		// TODO Auto-generated method stub
		return 100;
	}

	@Override
	@SCJAllowed(Level.SUPPORT)
	@SCJRestricted(phase = Phase.INITIALIZATION)
	public void initialize() {
		// TODO Auto-generated method stub
		
	}

}
