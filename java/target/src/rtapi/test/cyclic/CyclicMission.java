package test.cyclic;

import javax.realtime.PeriodicParameters;
import javax.realtime.PriorityParameters;
import javax.realtime.RelativeTime;
import javax.safetycritical.CyclicExecutive;
import javax.safetycritical.CyclicSchedule;
import javax.safetycritical.Frame;
import javax.safetycritical.PeriodicEventHandler;
import javax.safetycritical.StorageParameters;


public class CyclicMission extends CyclicExecutive {

	long missionMemory = 1024;
	int totalPeriodicHandlers = 3;
	
	// Maximum handler memory requirements, should be known in advance
	final int MAX_SIZE = 256;
	final int MAX_BS = 1024;

	@Override
	protected void initialize() {
		
		// Create the handlers for the frames
		peHandlerCount = totalPeriodicHandlers;
		for (int i = 0; i < peHandlerCount; i++) {
			(new EventHandler(new PriorityParameters(i + 10),
					new PeriodicParameters(null, new RelativeTime(10, 0)),
					new StorageParameters(1024, new long[] { 256 }), 128, "PEH"+i)).register();
		}
		
		maxHandlerSize = MAX_SIZE;
		maxHandlerBsSize = MAX_BS;
	}

	public CyclicSchedule getSchedule(PeriodicEventHandler[] handlers) {

		Frame[] frames = new Frame[3];
		frames[0] = new Frame(new RelativeTime(1500, 0),
				new PeriodicEventHandler[] { handlers[0], handlers[1] });
		frames[1] = new Frame(new RelativeTime(1500, 0),
				new PeriodicEventHandler[] { handlers[1], handlers[2] });
		frames[2] = new Frame(new RelativeTime(2000, 0),
				new PeriodicEventHandler[] { handlers[2], handlers[0] });

		return new CyclicSchedule(frames);

	}

	@Override
	public long missionMemorySize() {
		return missionMemory;
	}
	
	@Override
	public void cleanUp(){
		super.cleanUp();
	}

}
