package scopeuse.ex3;

import javax.realtime.PriorityParameters;
import javax.safetycritical.LinearMissionSequencer;
import javax.safetycritical.Mission;
import javax.safetycritical.MissionSequencer;
import javax.safetycritical.Safelet;
import javax.safetycritical.StorageParameters;

public class RetObjSafelet implements Safelet{
	
	@Override
	public MissionSequencer<Mission>getSequencer() {

		StorageParameters sp = new StorageParameters(1000000000, null, 0, 0);
		RetObjMission m = new RetObjMission();
		return new LinearMissionSequencer<Mission>(new PriorityParameters(13), sp, m); 
		
	}
	
	@Override
	public long immortalMemorySize() {
		
		return 100;
	}

}
