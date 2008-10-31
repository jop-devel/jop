/*
  This file is part of the TCK for JSR 302: Safety Critical JavaTM Technology
  	see <http://jcp.org/en/jsr/detail?id=302>

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
		
		// Those info will go away when we have more tests.
		// It's just here to make the output more interesting
		// and the tiny program look like they are doing some
		// 'real' testing ;-)
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
		return Safelet.LEVEL_1;
	}
}
