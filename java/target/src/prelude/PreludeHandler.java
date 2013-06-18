package prelude;

import javax.realtime.PeriodicParameters;
import javax.realtime.PriorityParameters;
import javax.realtime.RelativeTime;

import javax.safetycritical.Mission;
import javax.safetycritical.PeriodicEventHandler;
import javax.safetycritical.StorageParameters;

public class PreludeHandler extends PeriodicEventHandler {

	private PreludeTask task;

	public PreludeHandler(PreludeTask task) {
		super(new PriorityParameters(0),
			  new PeriodicParameters(new RelativeTime(task.initialRelease, 0),
									 new RelativeTime(task.period, 0),
									 new RelativeTime(task.deadline, 0), null),
			  new StorageParameters(PreludeSafelet.PRIVATE_MEMORY_SIZE, null),
			  PreludeSafelet.PRIVATE_MEMORY_SIZE);
		this.task = task;
	}

	@Override
	public void handleAsyncEvent() {
		task.run();
	}
}
