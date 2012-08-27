package test.level1;

import javax.realtime.PriorityParameters;
import javax.safetycritical.LinearMissionSequencer;
import javax.safetycritical.Mission;
import javax.safetycritical.MissionSequencer;
import javax.safetycritical.RepeatingMissionSequencer;
import javax.safetycritical.Safelet;
import javax.safetycritical.StorageParameters;

public class Level1Safelet implements Safelet{

	@Override
	public MissionSequencer<Mission> getSequencer() {

		PriorityParameters seq_prio = new PriorityParameters(13);
		long[] sizes = {1024};
		
		StorageParameters seq_storage = new StorageParameters(2048, sizes, 0, 0);

		MyMission m0 = new MyMission(0);
		MyMission m1 = new MyMission(1);
		
		Mission[] missions = new Mission[2];
		missions[0] = m0;
		missions[1] = m1;

//		return new LinearMissionSequencer<Mission>(seq_prio, seq_storage, missions);
		return new RepeatingMissionSequencer<Mission>(seq_prio, seq_storage, missions);
		
	
	}

	@Override
	public long immortalMemorySize() {
		return 10000;
	}
	
	

}
