/*
  This file is part of JOP, the Java Optimized Processor
    see <http://www.jopdesign.com/>

  Copyright (C) 2008, Martin Schoeberl (martin@jopdesign.com)

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

/**
 * 
 */
package javax.safetycritical;

import joprt.RtThread;
import joprt.SwEvent;

/**
 * @author Martin Schoeberl
 *
 */
public abstract class MissionDescriptor {

	private SwEvent clean;
	private boolean cleanupDidRun;
	
	// why is this static?
	// ok, in level 1 we have only one mission.
	static boolean terminationRequest = false;
	
	// perhaps this is a work-around at the moment
	protected MissionDescriptor() {

		// just an idle thread that watches the tremination request
		new RtThread(0, 10000) {
			public void run() {
				for (;;) {
					if (!cleanupDidRun && terminationRequest) {
						cleanup();
						cleanupDidRun = true;
					}
					waitForNextPeriod();
				}
			}
		};
		
//		clean = new SwEvent(0, 100) {
//			public void handle() {
//				if (!cleanupDidRun && terminationRequest) {
//					cleanup();
//				}
//			}
//		};
	}
	
	public final boolean terminationPending() {
		return terminationRequest;
	}

	public abstract long missionMemorySize();

	protected abstract void initialize();

	protected void cleanup() {
		// do nothing on default
	}

	public final void requestTermination() {
		terminationRequest = true;
		// It is simple polled in the PAEH - that'S easy ;-)
		// But we should also invoke cleanup().
		// That does not work when requestTermination is invoked
		// before startMission()
		// clean.fire();
	}
}
