package javax.safetycritical.test;

import javax.safetycritical.Mission;
import javax.safetycritical.MissionSequencer;
import javax.safetycritical.Safelet;

public class SafeletImpl implements Safelet<Mission> {

	@Override
	public MissionSequencer<Mission> getSequencer() {
		// TODO Auto-generated method stub
		return null;
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
