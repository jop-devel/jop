package test.cyclic;

import javax.realtime.PeriodicParameters;
import javax.realtime.PriorityParameters;
import javax.realtime.RelativeTime;
import javax.safetycritical.CyclicExecutive;
import javax.safetycritical.CyclicSchedule;
import javax.safetycritical.Frame;
import javax.safetycritical.ManagedMemory;
import javax.safetycritical.PeriodicEventHandler;
import javax.safetycritical.StorageParameters;


public class CyclicMission extends CyclicExecutive {

	long missionMemory = 1024;
	int totalPeriodicHandlers = 3;
	
	@Override
	protected void initialize() {
		
		// Create the handlers for the frames
//		peHandlerCount = totalPeriodicHandlers;
		for (int i = 0; i < totalPeriodicHandlers; i++) {
			(new EventHandler(new PriorityParameters(i + 10),
					new PeriodicParameters(null, new RelativeTime(10, 0)),
					new StorageParameters(1024, null), 256, "PEH"+i)).register();
		}
	}

	public CyclicSchedule getSchedule(PeriodicEventHandler[] handlers) {
		
		Frame[] frames = new Frame[100];
		for (int i = 0; i < frames.length; i++) {
			frames[i] = new Frame(new RelativeTime(1500, 0),
					new PeriodicEventHandler[] {
							handlers[i % totalPeriodicHandlers],
							handlers[(i + 1) % totalPeriodicHandlers] });
		}
		
//		Frame[] frames = new Frame[3];
//		frames[0] = new Frame(new RelativeTime(1500, 0),
//				new PeriodicEventHandler[] { handlers[0], handlers[1] });
//		frames[1] = new Frame(new RelativeTime(1500, 0),
//				new PeriodicEventHandler[] { handlers[1], handlers[2] });
//		frames[2] = new Frame(new RelativeTime(2000, 0),
//				new PeriodicEventHandler[] { handlers[2], handlers[0] });

		return new CyclicSchedule(frames);

	}

	@Override
	public long missionMemorySize() {
		return missionMemory;
	}
	
	@Override
	public void cleanUp(){
		
		ImmortalEntry.dumpLog.selector = 0;
		for(int i =0; i < ImmortalEntry.eventsLogged; i++){
			ImmortalEntry.dumpLog.logEntry = i;
			ManagedMemory.enterPrivateMemory(1500, ImmortalEntry.dumpLog);
		}
		
		ImmortalEntry.dumpLog.selector = 1;
		for(int i =0; i < ImmortalEntry.eventsLogged-1; i++){
			ImmortalEntry.dumpLog.logEntry = i;
			ManagedMemory.enterPrivateMemory(1500, ImmortalEntry.dumpLog);
		}
		
		ImmortalEntry.dumpLog.selector = 2;
		for(int i =0; i < ImmortalEntry.eventsLogged-1; i++){
			ImmortalEntry.dumpLog.logEntry = i;
			ManagedMemory.enterPrivateMemory(1500, ImmortalEntry.dumpLog);
		}

		super.cleanUp();
	}

}
