package test.cyclic;

import static javax.safetycritical.annotate.Level.SUPPORT;
import static javax.safetycritical.annotate.Phase.INITIALIZATION;

import javax.realtime.PriorityParameters;
import javax.safetycritical.CyclicExecutive;
import javax.safetycritical.MissionSequencer;
import javax.safetycritical.Safelet;
import javax.safetycritical.StorageParameters;
import javax.safetycritical.annotate.SCJAllowed;
import javax.safetycritical.annotate.SCJRestricted;

public class CyclicSafelet implements Safelet<CyclicExecutive>{

	@Override
	public MissionSequencer<CyclicExecutive> getSequencer() {
		
		PriorityParameters sequencerPrio = new PriorityParameters(10);
		StorageParameters sequencerSto = new StorageParameters(1024, new long[] {512});
		return new SingleMissionSequencer(sequencerPrio, sequencerSto);
		
	}
	
	@Override
	public long immortalMemorySize() {
		return 0;
	}
	
	class SingleMissionSequencer extends MissionSequencer<CyclicExecutive> {

		boolean served = false;

		public SingleMissionSequencer(PriorityParameters priority,
				StorageParameters storage) {
			super(priority, storage);
		}
		
		CyclicExecutive newMission() {
			
			CyclicExecutive single = new CyclicMission();
			return single;
		}

		@SCJAllowed(SUPPORT)
		@SCJRestricted(phase = INITIALIZATION, maySelfSuspend = false)
		@Override
		protected CyclicExecutive getNextMission() {
			if(!served){
				current_mission = newMission();
				
				// Comment the following line to have an infinite
				// stream of missions
				served = true;
				
				return (CyclicExecutive) current_mission;
			}
			
			current_mission = null;
			return null;
		}

	}


}
