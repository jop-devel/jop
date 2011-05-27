package javax.safetycritical.test;

import javax.safetycritical.Mission;
import javax.safetycritical.Safelet;

public class SafeletImpl implements Safelet<Mission> {

	MissionSequencerImpl missionSequencer;
	
	public SafeletImpl() {
		missionSequencer = new MissionSequencerImpl(null, null);
	}
	
	@Override
	public MissionSequencerImpl getSequencer() {
		return missionSequencer;
	}
	
}
