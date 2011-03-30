package javax.safetycritical.test;

import javax.realtime.PriorityParameters;
import javax.safetycritical.Mission;
import javax.safetycritical.MissionSequencer;
import javax.safetycritical.StorageParameters;

// Contains only a single mission since this is level 1
public class MissionSequencerImpl extends MissionSequencer<Mission> {

	private MissionImpl currentMission;
	
	public MissionSequencerImpl(PriorityParameters priority,
			StorageParameters storage) {
		super(priority, storage);
		currentMission = new MissionImpl();
		currentMission.initialize();
	}

	@Override
	protected MissionImpl getNextMission() {
		return currentMission;
	}

}
