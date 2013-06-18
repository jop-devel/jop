package prelude;

import javax.realtime.PriorityParameters;
import javax.safetycritical.Mission;
import javax.safetycritical.MissionSequencer;
import javax.safetycritical.StorageParameters;

public class PreludeSequencer extends MissionSequencer {

	Mission m;
	
	public PreludeSequencer(Mission mission) {
		super(new PriorityParameters(13),
			  new StorageParameters(PreludeSafelet.MISSION_MEMORY_SIZE, null),
			  "Prelude sequencer");
		m = mission;
	}

	@Override
	protected Mission getNextMission() {
		return m;
	}

}
