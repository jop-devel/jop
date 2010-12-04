package javax.safetycritical.test;

import javax.safetycritical.PeriodicEventHandler;

public class PeriodicEventHandlerImpl extends PeriodicEventHandler {
	
	String message;

	public PeriodicEventHandlerImpl(int priority, int period, String message) {
		super(null, null, null);
		this.message = message;
		new RtThreadImpl(priority, period, this);
	}

	@Override
	public void handleAsyncEvent() {
		System.out.print(message);
	}
}
