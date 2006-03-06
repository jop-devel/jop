package ravenscar;

import joprt.*;
// import javax.realtime.*;
// public class PeriodicThread extends NoHeapRealtimeThread

public class PeriodicThread extends RtThread {

	// constructors
	public PeriodicThread(PriorityParameters pp, PeriodicParameters p,
											 java.lang.Runnable logic) 
	{
		// super(pp, p, ImmortalMemory.instance());
		super(pp.getPriority(),
			p.getPeriod().getUs(),
			p.getEpoch().getUs());

		applicationLogic = logic;
	}

	private java.lang.Runnable applicationLogic;
	
	// methods

	public void run() {

		boolean noProblems = true;
		while(noProblems) {
		 // System.out.println("periodic thread looping");
		 // System.out.println("noProblem is " + noProblems);
			applicationLogic.run();
			noProblems = waitForNextPeriod();
		}
		System.out.println("Deadline missed!!");
		for (;;) waitForNextPeriod();
		// System.out.println("Deadline is missed!!!");
		// A deadline has been missed, or a cost
		// overrun has occured and there are no handlers.
		// If Ravenscar-RTSJ allows recovery, it would be called here
	}

	/*
	public static RealtimeThread currentPeriodicRealtimeThread()
	{ return RealtimeThread.currentRealtimeThread();};
	
	public MemoryArea getMemoryArea()
	{ return super.getMemoryArea();};
	*/
	
	public void start() {

		// nothing to do in joprt
		// super.start();
	}
}
