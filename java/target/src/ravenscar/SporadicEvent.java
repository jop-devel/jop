package ravenscar;

// import javax.realtime.*;
// public class SporadicEvent extends javax.realtime.AsyncEvent

// import joprt.*;

public class SporadicEvent {

	SporadicEventHandler myHandler;

	public SporadicEvent(SporadicEventHandler handler)
	{
		// super();
		// super.addHandler(handler);
		myHandler = handler;
	}
	
	public void fire()
	{
		// super.fire();
		myHandler.fire();
	}
	
};

