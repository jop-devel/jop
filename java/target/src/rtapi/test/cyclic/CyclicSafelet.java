package test.cyclic;

import static javax.safetycritical.annotate.Level.SUPPORT;
import static javax.safetycritical.annotate.Phase.INITIALIZATION;

import javax.realtime.PriorityParameters;
import javax.safetycritical.Mission;
import javax.safetycritical.MissionSequencer;
import javax.safetycritical.Safelet;
import javax.safetycritical.StorageParameters;
import javax.safetycritical.annotate.SCJAllowed;
import javax.safetycritical.annotate.SCJRestricted;

public class CyclicSafelet implements Safelet<Mission>{

	@Override
	public MissionSequencer<Mission> getSequencer() {
		
		Mission m = new MyCyclicMission();
		
		PriorityParameters mission_prio = new PriorityParameters(10);
		StorageParameters mission_sto = new StorageParameters(1024, new long[] {512});
		return new SingleMissionSequencer(mission_prio, mission_sto, m);
		
	}
	
	@Override
	public long immortalMemorySize() {
		// TODO Auto-generated method stub
		return 0;
	}
	
	class SingleMissionSequencer extends MissionSequencer<Mission> {

		Mission single;

		public SingleMissionSequencer(PriorityParameters priority,
				StorageParameters storage, Mission m) {
			super(priority, storage);
			single = m;
		}

		@SCJAllowed(SUPPORT)
		@SCJRestricted(phase = INITIALIZATION, maySelfSuspend = false)
		@Override
		protected Mission getNextMission() {
			return single;
		}

	}


}
