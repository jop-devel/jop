/*
  This file is part of the SCJ TCK

  Copyright (C) 2008, The Open Group
  Author: Martin Schoeberl (martin@jopdesign.com)

  License TBD.
*/


/**
 * 
 */
package scjtck;

import javax.realtime.PriorityParameters;
import javax.realtime.PriorityScheduler;
import javax.realtime.RelativeTime;
import javax.safetycritical.PeriodicEventHandler;
import javax.safetycritical.PeriodicParameters;
import javax.safetycritical.Safelet;
import javax.safetycritical.Terminal;
import javax.safetycritical.ThreadConfiguration;

/**
 * @author Martin Schoeberl
 *
 */
public class TestTermination extends TestCase implements Safelet {
	
	public String getName() {
		return "Test termination";
	}
	
	boolean pehDidRun;

	protected void initialize() {
		
		info("You should NOT see the PEH 'Ping' message.");
		
		new PeriodicEventHandler(
				new PriorityParameters(PriorityScheduler.getMaxPriority()),
				new PeriodicParameters(new RelativeTime(0,0), new RelativeTime(1000,0)),
				new ThreadConfiguration()
			) {
			public void handleAsyncEvent() {
				pehDidRun = true;
				Terminal.getTerminal().writeln("Ping ");
			}
		};
		
		info("request termination");
		requestTermination();
		info("after request");
	}

	protected void cleanup() {
		test(pehDidRun==false);
		finish();
	}
	
	public long missionMemorySize() {
		return 0;
	}

	public int getLevel() {
		return 1;
	}
}
