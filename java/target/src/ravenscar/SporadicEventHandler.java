package ravenscar;

import joprt.*;

// import javax.realtime.*;
// public class SporadicEventHandler extends javax.realtime.BoundAsyncEventHandler {
public class SporadicEventHandler extends SwEvent {

	public SporadicEventHandler(PriorityParameters pp, SporadicParameters spor) {

		// super(pri, spor, null, ImmortalMemory.instance(),null, true, null); // no heap

		super(pp.getPriority(),
				spor.getMinInterarrival().getUs());

	}
	
	// override SwEvents handle method to call jagun's handle-version
	public void handle() {

		handleAsyncEvent();
	}

	/*
	public MemoryArea getMemoryArea()
	{
		return super.getMemoryArea();
	}
	*/
	
	// ovveride to handle event
	
	public void handleAsyncEvent() { }
};

