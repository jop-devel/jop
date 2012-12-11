package scopeuse.ex1;

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

import com.jopdesign.sys.GC;
import com.jopdesign.sys.Native;

public class LoopSafelet implements Safelet{
	
	@Override
	public MissionSequencer<Mission>getSequencer() {

		StorageParameters sp = new StorageParameters(1000000000, null, 0, 0);
		LoopMission m = new LoopMission();
		return new LinearMissionSequencer<Mission>(new PriorityParameters(13), sp, false, m); 
		
	}
	
	public void setup(){
		
		// Block of data to be encoded
		Data.init(100);
		
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
