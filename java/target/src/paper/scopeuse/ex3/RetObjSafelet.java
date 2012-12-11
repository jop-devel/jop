package scopeuse.ex3;

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

public class RetObjSafelet implements Safelet{
	
	@Override
	public MissionSequencer<Mission>getSequencer() {

		StorageParameters sp = new StorageParameters(1000000000, null, 0, 0);
		RetObjMission m = new RetObjMission();
		return new LinearMissionSequencer<Mission>(new PriorityParameters(13), sp, false, m); 
		
	}
	
	@Override
	public long immortalMemorySize() {
		
		return 100;
	}

	@Override
	@SCJAllowed(Level.SUPPORT)
	@SCJRestricted(phase = Phase.INITIALIZATION)
	public void initializeApplication() {
		// TODO Auto-generated method stub
		
	}

}
