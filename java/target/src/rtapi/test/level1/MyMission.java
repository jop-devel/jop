package test.level1;

import javax.realtime.AperiodicParameters;
import javax.realtime.PeriodicParameters;
import javax.realtime.PriorityParameters;
import javax.realtime.RelativeTime;
import javax.safetycritical.Mission;
import javax.safetycritical.StorageParameters;

public class MyMission extends Mission{
	
	int number;
	int totalPeriodicHandlers = 1;
	int totalAperiodicHandlers = 1;
	int totalAperiodicLongHandlers = 1;
	
	MyMission(int number){
		
		this.number = number;
		
	}

	@Override
	protected void initialize() {
		
		TestPEH peh;
		TestAEH aeh;
		TestALEH aleh;
		
		System.out.println("Mission " +number+ " initialization");
//		peHandlerCount = totalPeriodicHandlers;
//		aeHandlerCount = totalAperiodicHandlers;
//		aleHandlerCount = totalAperiodicLongHandlers;
		
		PriorityParameters eh1_prio = new PriorityParameters(14);
		AperiodicParameters eh1_pparams = new AperiodicParameters(null, null);
		
		StorageParameters eh1_storage = new StorageParameters(1024, null, 0, 0);
		aeh = new TestAEH(eh1_prio, eh1_pparams, eh1_storage, 512, "Aperiodic Handler 1");
		aeh.register();

		aleh = new TestALEH(eh1_prio, eh1_pparams, eh1_storage, 512, "Aperiodic Long Handler");
		aleh.register();

		PriorityParameters eh0_prio = new PriorityParameters(13);
		RelativeTime eh0_tart = new RelativeTime(0,0);
		RelativeTime eh0_period = new RelativeTime(1000, 0);
		PeriodicParameters eh0_pparams = new PeriodicParameters(eh0_tart, eh0_period);
		
		
		StorageParameters eh0_storage = new StorageParameters(1024, null, 0, 0);
		
		peh = new TestPEH(eh0_prio, eh0_pparams, eh0_storage, 512, aeh,aleh);
		
		peh.register();
		

		
	}

	@Override
	public long missionMemorySize() {
		// TODO Auto-generated method stub
		return 0;
	}

}
