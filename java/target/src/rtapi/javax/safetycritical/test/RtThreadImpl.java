package javax.safetycritical.test;

import joprt.RtThread;

public class RtThreadImpl extends RtThread{

	PeriodicEventHandlerImpl handlerImpl;
	
	public RtThreadImpl(int prio, int us, PeriodicEventHandlerImpl handlerImpl) {
		super(prio, us);
		this.handlerImpl = handlerImpl;
	}
	
	@Override
	public void run() {
		super.run();
		while(true)
		{
			handlerImpl.handleAsyncEvent();
			this.waitForNextPeriod();
		}
	}
	
}
