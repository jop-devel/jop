package scopeuse.ex1;

import javax.realtime.PriorityParameters;
import javax.safetycritical.LinearMissionSequencer;
import javax.safetycritical.Mission;
import javax.safetycritical.MissionSequencer;
import javax.safetycritical.Safelet;
import javax.safetycritical.StorageParameters;

import com.jopdesign.sys.GC;
import com.jopdesign.sys.Native;

public class LoopSafelet implements Safelet{
	
	@Override
	public MissionSequencer<Mission>getSequencer() {

		StorageParameters sp = new StorageParameters(1000000000, null, 0, 0);
		LoopMission m = new LoopMission();
		return new LinearMissionSequencer<Mission>(new PriorityParameters(13), sp, m); 
		
	}
	
	public void setup(){
		
		// Block of data to be encoded
		Data.init(100);
		
	}

	@Override
	public long immortalMemorySize() {
		
		return 100;
	}

}
