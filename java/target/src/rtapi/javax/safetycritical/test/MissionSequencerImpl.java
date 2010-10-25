package javax.safetycritical.test;

import javax.realtime.PriorityParameters;
import javax.safetycritical.Mission;
import javax.safetycritical.MissionSequencer;
import javax.safetycritical.StorageParameters;

public class MissionSequencerImpl extends MissionSequencer<Mission> {

	private Mission[] missions;
	private int currentMission = 0;
	
	public MissionSequencerImpl(PriorityParameters priority,
			StorageParameters storage) {
		super(priority, storage);
		
		
		
	}

	@Override
	protected Mission getNextMission() {
		// TODO Auto-generated method stub
		currentMission++;
		if(currentMission > 1)
		{
			currentMission = 0;
		}
		return missions[currentMission];
	}

}
