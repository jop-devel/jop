package test.level1;

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
		
		System.out.println("Mission "+number+ " initialization");
		
		PriorityParameters eh0_prio = new PriorityParameters(13);
		RelativeTime eh0_tart = new RelativeTime(0,0);
		RelativeTime eh0_period = new RelativeTime(1000, 0);
		PeriodicParameters eh0_pparams = new PeriodicParameters(eh0_tart, eh0_period);
		
		StorageParameters eh0_storage = new StorageParameters(1024, null, 0, 0);
		
		EventHandler0 EH0 = new EventHandler0(eh0_prio, eh0_pparams, eh0_storage, 512);
		EH0.register();
	}

	@Override
	public long missionMemorySize() {
		// TODO Auto-generated method stub
		return 0;
	}

}
