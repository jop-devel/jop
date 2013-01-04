package csp.scj.watchdog;

import javax.safetycritical.ManagedMemory;
import javax.safetycritical.Mission;
import javax.safetycritical.annotate.Level;
import javax.safetycritical.annotate.SCJAllowed;

import csp.Constants;
import csp.ImmortalEntry;
import csp.InterruptTask;
import csp.PollingTask;

public class WatchDogMission extends Mission {

	@Override
	protected void initialize() {

		// Watchdog task
		WatchdogHandler WD = new WatchdogHandler(Constants.WD_TIMEOUT, ImmortalEntry.slaves);
		WD.register();

		if (Constants.POLLED_MODE) {
			// Polling task
			PollingTask PT = new PollingTask();
			PT.register();
		} else {
			// Interrupt task
			// int 1 = i2c_a
			// int 2 = i2c_b
			InterruptTask IT = new InterruptTask();
			IT.register(2);
		}

		ImmortalEntry.log.addEvent("Buff. Pool: "
				+ ImmortalEntry.bufferPool.getOccupancy());
	}

	@Override
	public long missionMemorySize() {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	@SCJAllowed(Level.SUPPORT)
	protected void cleanUp() {
		dumpLog();
	}

	private void dumpLog() {

		// ImmortalEntry.dumpLog.selector = 0;
		// for (int i = 0; i < ImmortalEntry.eventsLogged; i++) {
		// ImmortalEntry.dumpLog.logEntry = i;
		// ManagedMemory.enterPrivateMemory(1500, ImmortalEntry.dumpLog);
		// }

		ImmortalEntry.dumpLog.selector = 1;
		for (int i = 0; i < ImmortalEntry.slaves.length; i++) {
			ImmortalEntry.dumpLog.logEntry = i;
			ManagedMemory.enterPrivateMemory(1500, ImmortalEntry.dumpLog);
		}

	}
}
