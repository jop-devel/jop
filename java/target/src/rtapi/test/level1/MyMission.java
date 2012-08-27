package test.level1;

import javax.realtime.AperiodicParameters;
import javax.realtime.PeriodicParameters;
import javax.realtime.PriorityParameters;
import javax.realtime.RelativeTime;
import javax.safetycritical.Mission;
import javax.safetycritical.StorageParameters;

public class MyMission extends Mission{
	
	int number;
	
	MyMission(int number){
		
		this.number = number;
		
	}

	@Override
	protected void initialize() {
		
		phase = Mission.INITIALIZATION;
		
		TestPEH EH0;
		TestAEH EH1;
		
		System.out.println("Mission "+number+ " initialization");
		
		long[] sizes = {512};
		PriorityParameters eh1_prio = new PriorityParameters(14);
		AperiodicParameters eh1_pparams = new AperiodicParameters(null, null);
		
		StorageParameters eh1_storage = new StorageParameters(1024, sizes, 0, 0);
		EH1 = new TestAEH(eh1_prio, eh1_pparams, eh1_storage, "Aperiodic Handler 1");
		EH1.register();
		
		PriorityParameters eh0_prio = new PriorityParameters(13);
		RelativeTime eh0_tart = new RelativeTime(0,0);
		RelativeTime eh0_period = new RelativeTime(1000, 0);
		PeriodicParameters eh0_pparams = new PeriodicParameters(eh0_tart, eh0_period);
		
		
		StorageParameters eh0_storage = new StorageParameters(1024, sizes, 0, 0);
		
		EH0 = new TestPEH(eh0_prio, eh0_pparams, eh0_storage, 512, EH1);
		EH0.register();
		

		
	}

	@Override
	public long missionMemorySize() {
		// TODO Auto-generated method stub
		return 0;
	}

}
