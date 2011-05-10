package javax.safetycritical.test;

import javax.safetycritical.PeriodicEventHandler;

import joprt.RtThread;

public class RtThreadImpl extends RtThread{

	PeriodicEventHandler PEH;
	
	public RtThreadImpl(int prio, int us, PeriodicEventHandler handlerImpl) {
		super(prio, us);
		this.PEH = handlerImpl;
	}
	
	public void run() {
		while(true)
		{
			PEH.handleAsyncEvent();
			this.waitForNextPeriod();
		}
	}
}
