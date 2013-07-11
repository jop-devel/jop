package test.cyclic;

import static javax.safetycritical.annotate.Level.SUPPORT;
import static javax.safetycritical.annotate.Phase.INITIALIZATION;

import javax.realtime.PriorityParameters;
import javax.safetycritical.CyclicExecutive;
import javax.safetycritical.Mission;
import javax.safetycritical.MissionSequencer;
import javax.safetycritical.Safelet;
import javax.safetycritical.StorageParameters;
import javax.safetycritical.annotate.Level;
import javax.safetycritical.annotate.Phase;
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
		Mission mission;

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
				mission = newMission();
				
				// Comment the following line to have an infinite
				// stream of missions
				served = true;
				
				return (CyclicExecutive) mission;
			}
			
			mission = null;
			return null;
		}

	}

	@Override
	@SCJAllowed(Level.SUPPORT)
	@SCJRestricted(phase = Phase.INITIALIZATION)
	public void initializeApplication() {
		ImmortalEntry.setup();
		
	}


}
