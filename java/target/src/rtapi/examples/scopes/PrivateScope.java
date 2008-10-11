package examples.scopes;

import javax.realtime.*;

import joprt.RtThread;

public class PrivateScope extends LTPhysicalMemory {
	
	static RuntimeException notOwner = new RuntimeException("Not the current owner");

//	private RealtimeThread owner;
	private RtThread owner;

	public PrivateScope(long size) {
		super(PhysicalMemoryManager.ON_CHIP_PRIVATE, size);
		owner = RtThread.currentRtThread();
//		this.owner = RealtimeThread.currentRealtimeThread();
	}

	public void enter(Runnable R) {
//		if (RealtimeThread.currentRealtimeThread() != owner) {
		if (RtThread.currentRtThread() != owner) {
			// the calling thread doesn't own this scope
			throw notOwner;
		} else {
			super.enter(R);
		}
	}

	// similarly for executeInArea, newArray and
	// newInstance etc

	//	  . . .
}
