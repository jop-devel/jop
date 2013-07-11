package scjlibs;

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

import com.jopdesign.sys.Memory;

public class GenericSafelet implements Safelet<GenericMission>{

	@Override
	@SCJAllowed(Level.SUPPORT)
	@SCJRestricted(phase = Phase.INITIALIZATION)
	public void initializeApplication() {
		
		ImmortalEntry.setup();
		
	}

	@Override
	@SCJAllowed(Level.SUPPORT)
	@SCJRestricted(phase = Phase.INITIALIZATION)
	public MissionSequencer<GenericMission> getSequencer() {
		
		PriorityParameters seq_prio = new PriorityParameters(13);
		GenericMission mission = new PropagateExceptionMission(); //TestMission();
		StorageParameters seq_storage = new StorageParameters(2048, null, 0, 0);
		return new LinearMissionSequencer<GenericMission>(seq_prio, seq_storage, false, mission);
	}

	@Override
	@SCJAllowed(Level.SUPPORT)
	public long immortalMemorySize() {
		// TODO Auto-generated method stub
		return 0;
	}

}
