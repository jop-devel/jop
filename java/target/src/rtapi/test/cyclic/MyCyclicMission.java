package test.cyclic;

import javax.realtime.PeriodicParameters;
import javax.realtime.PriorityParameters;
import javax.realtime.RelativeTime;
import javax.safetycritical.CyclicExecutive;
import javax.safetycritical.CyclicSchedule;
import javax.safetycritical.Frame;
import javax.safetycritical.MissionSequencer;
import javax.safetycritical.PeriodicEventHandler;
import javax.safetycritical.StorageParameters;

import cmp.Execute;

public class MyCyclicMission extends CyclicExecutive {

	PeriodicEventHandler[] handlers;
	int numHandlers = 3;

	@Override
	protected void initialize() {

		for (int i = 0; i < numHandlers - 1; i++) {
			handlers[i] = new EventHandler(new PriorityParameters(i + 10),
					new PeriodicParameters(null, new RelativeTime(10, 0)),
					new StorageParameters(1024, new long[] { 256 }), 0);
		}
		
		startCycle();
	}

	public CyclicSchedule getSchedule(PeriodicEventHandler[] handlers) {

		Frame[] frames = new Frame[3];

		frames[0] = new Frame(new RelativeTime(500, 0),
				new PeriodicEventHandler[] { handlers[0], handlers[1] });
		frames[1] = new Frame(new RelativeTime(250, 0),
				new PeriodicEventHandler[] { handlers[1], handlers[2] });
		frames[2] = new Frame(new RelativeTime(125, 0),
				new PeriodicEventHandler[] { handlers[2], handlers[0] });

		return new CyclicSchedule(frames);

	}

	@Override
	public long missionMemorySize() {
		// TODO Auto-generated method stub
		return 0;
	}

}
