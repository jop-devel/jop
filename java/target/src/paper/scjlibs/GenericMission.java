package scjlibs;

import javax.safetycritical.Mission;
import javax.safetycritical.annotate.SCJAllowed;

public abstract class GenericMission extends Mission{

	@Override
	@SCJAllowed
	public long missionMemorySize() {
		// TODO Auto-generated method stub
		return 0;
	}

}
