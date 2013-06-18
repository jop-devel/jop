package prelude;

import javax.safetycritical.Mission;

public class PreludeMission extends Mission {

	PreludeTask [] taskSet;
	PreludePrecedence [] precSet;	
	
	public PreludeMission(PreludeTask [] taskSet, 
						  PreludePrecedence [] precSet) {
		this.taskSet = taskSet;
		this.precSet = precSet;
	}

	@Override
	protected void initialize() {
		for (int i = 0; i < taskSet.length; i++) {
			PreludeHandler h = new PreludeHandler(taskSet[i]);
			h.register();
		}
	}

	@Override
	public long missionMemorySize() {
		return PreludeSafelet.MISSION_MEMORY_SIZE;
	}
}
