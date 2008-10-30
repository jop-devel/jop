/*
  This file is part of JOP, the Java Optimized Processor
    see <http://www.jopdesign.com/>

  Copyright (C) 2001-2008, Martin Schoeberl (martin@jopdesign.com)

  This program is free software: you can redistribute it and/or modify
  it under the terms of the GNU General Public License as published by
  the Free Software Foundation, either version 3 of the License, or
  (at your option) any later version.

  This program is distributed in the hope that it will be useful,
  but WITHOUT ANY WARRANTY; without even the implied warranty of
  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  GNU General Public License for more details.

  You should have received a copy of the GNU General Public License
  along with this program.  If not, see <http://www.gnu.org/licenses/>.
*/

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
				((int) p.getPeriod().getMilliseconds())*1000 + p.getPeriod().getNanoseconds()/1000,
				((int) p.getEpoch().getMilliseconds())*1000 + p.getEpoch().getNanoseconds()/1000
			);
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
