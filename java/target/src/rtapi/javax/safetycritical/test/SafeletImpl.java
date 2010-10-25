package javax.safetycritical.test;

import javax.safetycritical.Mission;
import javax.safetycritical.MissionSequencer;
import javax.safetycritical.Safelet;

public class SafeletImpl implements Safelet<Mission> {

	MissionSequencer<Mission> missionSequencer = new MissionSequencerImpl(null, null);
	
	@Override
	public MissionSequencer<Mission> getSequencer() {
		// TODO Auto-generated method stub
		return missionSequencer;
	}

	@Override
	public void setUp() {
		// Here obejcts may be put into ImmortalMemoryArea
		
	}

	@Override
	public void tearDown() {
		// TODO Auto-generated method stub
		
	}

}
