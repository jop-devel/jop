package test.level1;

import java.util.Random;

import javax.realtime.PeriodicParameters;
import javax.realtime.PriorityParameters;
import javax.safetycritical.AperiodicEventHandler;
import javax.safetycritical.Mission;
import javax.safetycritical.PeriodicEventHandler;
import javax.safetycritical.StorageParameters;
import javax.safetycritical.Terminal;

public class TestPEH extends PeriodicEventHandler {
	
	int fireCount = 0;
	AperiodicEventHandler aeh;
	
	Random rnd = new Random();

	public TestPEH(PriorityParameters priority,
			PeriodicParameters parameters, StorageParameters scp, long scopeSize, AperiodicEventHandler aeh) {
		super(priority, parameters, scp, scopeSize);
		this.aeh = aeh;
	}

	@Override
	public void handleAsyncEvent() {
		
		Terminal.getTerminal().writeln("PEH");

		if (rnd.nextInt(3) == 1){
			aeh.release();
			fireCount++;
		}
		
		if (fireCount > 2){
			Mission.getCurrentMission().requestTermination();
		}
		
	}

}
