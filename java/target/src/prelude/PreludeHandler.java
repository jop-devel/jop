package prelude;

import javax.realtime.PeriodicParameters;
import javax.realtime.PriorityParameters;
import javax.realtime.Schedulable;

import javax.safetycritical.Mission;
import javax.safetycritical.PeriodicEventHandler;
import javax.safetycritical.StorageParameters;

public class PreludeHandler extends PeriodicEventHandler {

	private PreludeTask task;
	private PeriodicParameters periodicParams;

	static int prio;

	public PreludeHandler(PreludeTask task, PeriodicParameters params, int core) {
		super(new PriorityParameters(prio++), params,
			  new StorageParameters(PreludeSafelet.PRIVATE_MEMORY_SIZE, null),
			  PreludeSafelet.PRIVATE_MEMORY_SIZE,
			  task.name);
		this.task = task;
		this.periodicParams = params;
		this.thread.setProcessor(core);
	}

	public PeriodicParameters getPeriodicParams() {
		return periodicParams;
	}

	@Override
	public void handleAsyncEvent() {
		task.run();
	}

	public Schedulable getThread() {
		return thread;
	}
}
