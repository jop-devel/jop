package test.level1;

import javax.realtime.PeriodicParameters;
import javax.realtime.PriorityParameters;
import javax.safetycritical.AperiodicEventHandler;
import javax.safetycritical.Mission;
import javax.safetycritical.PeriodicEventHandler;
import javax.safetycritical.StorageParameters;
import javax.safetycritical.Terminal;

public class TestPEH extends PeriodicEventHandler {
	
	int count = 0;
	AperiodicEventHandler aeh;

	public TestPEH(PriorityParameters priority,
			PeriodicParameters parameters, StorageParameters scp, long scopeSize, AperiodicEventHandler aeh) {
		super(priority, parameters, scp, scopeSize);
		this.aeh = aeh;
	}

	@Override
	public void handleAsyncEvent() {
		
		if (count < 5){
			Terminal.getTerminal().writeln("PEH");
			count++;
			aeh.release();
			
		}else{
			Mission.getCurrentMission().requestTermination();
		}
		
		
		
	}

}
