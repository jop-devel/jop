package test.level1;

import javax.realtime.PeriodicParameters;
import javax.realtime.PriorityParameters;
import javax.safetycritical.Mission;
import javax.safetycritical.PeriodicEventHandler;
import javax.safetycritical.StorageParameters;
import javax.safetycritical.Terminal;

public class EventHandler0 extends PeriodicEventHandler {
	
	int count = 0; 

	public EventHandler0(PriorityParameters priority,
			PeriodicParameters parameters, StorageParameters scp, long scopeSize) {
		super(priority, parameters, scp, scopeSize);
	}

	@Override
	public void handleAsyncEvent() {
		
		if (count < 5){
			Terminal.getTerminal().writeln("hello from handler");
			count++;
		}else{
			Mission.getCurrentMission().requestTermination();
		}
		
		
		
	}

}
