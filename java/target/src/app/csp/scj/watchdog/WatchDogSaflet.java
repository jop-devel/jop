package csp.scj.watchdog;

import javax.realtime.PriorityParameters;
import javax.safetycritical.LinearMissionSequencer;
import javax.safetycritical.Mission;
import javax.safetycritical.MissionSequencer;
import javax.safetycritical.Safelet;
import javax.safetycritical.StorageParameters;
import javax.safetycritical.annotate.Level;
import javax.safetycritical.annotate.Phase;
import javax.safetycritical.annotate.SCJAllowed;
import javax.safetycritical.annotate.SCJRestricted;

import csp.ImmortalEntry;

public class WatchDogSaflet implements Safelet<Mission>{

	@Override
	@SCJAllowed(Level.SUPPORT)
	@SCJRestricted(phase = Phase.INITIALIZATION)
	public void initializeApplication() {
		ImmortalEntry.setup();
		ImmortalEntry.initialParams();
		
	}

	@Override
	public MissionSequencer<Mission> getSequencer() {

		StorageParameters sp = new StorageParameters(1024, null);
		WatchDogMission m = new WatchDogMission();
		return new LinearMissionSequencer<Mission>(new PriorityParameters(13), sp, false, m);
	}

	@Override
	public long immortalMemorySize() {
		// TODO Auto-generated method stub
		return 100;
	}

}
