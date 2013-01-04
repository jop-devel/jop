package csp;

import javax.realtime.PeriodicParameters;
import javax.realtime.PriorityParameters;
import javax.realtime.RelativeTime;
import javax.safetycritical.PeriodicEventHandler;
import javax.safetycritical.StorageParameters;
import javax.safetycritical.annotate.Level;
import javax.safetycritical.annotate.SCJAllowed;

import util.Timer;

public class PollingTask extends PeriodicEventHandler {

	int dataCount;

	public PollingTask() {

		super(new PriorityParameters(12), new PeriodicParameters(
				new RelativeTime(0, 0), new RelativeTime(2, 0)),
				new StorageParameters(512, null), 256, "PO_Task");

	}

	@Override
	@SCJAllowed(Level.SUPPORT)
	public void handleAsyncEvent() {

		// Timer.wd();

		// For debug purposes, use i2c_b instead of i2c_a
		Services.receivePacket(ImmortalEntry.i2c_b);

		ImmortalEntry.log.addEvent(getName());

		// Timer.wd();
	}
}
