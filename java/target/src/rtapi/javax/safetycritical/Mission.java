/*
  This file is part of JOP, the Java Optimized Processor
    see <http://www.jopdesign.com/>

  Copyright (C) 2008-2011, Martin Schoeberl (martin@jopdesign.com)

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

package javax.safetycritical;

import static javax.safetycritical.annotate.Level.LEVEL_1;
import static javax.safetycritical.annotate.Level.SUPPORT;

import javax.safetycritical.annotate.Allocate;
import javax.safetycritical.annotate.SCJAllowed;
import javax.safetycritical.annotate.Allocate.Area;

/**
 * 
 * @author martin
 * 
 */
@SCJAllowed
public abstract class Mission {
	
	// To keep track of the state of a mission
	public static final int INACTIVE = 0;
	public static final int INITIALIZATION = 1;
	public static final int EXECUTION = 2;
	public static final int CLEANUP = 3;
	
	public int phase = INACTIVE;

	@Allocate( { Area.THIS })
	@SCJAllowed
	public Mission() {
	}

	// why is this SUPPORT?
	@SCJAllowed(SUPPORT)
	protected abstract void initialize();

	@SCJAllowed
	abstract public long missionMemorySize();

	@SCJAllowed(SUPPORT)
	protected void cleanUp() {
		System.out.println("Mission cleanup");
	}

	@SCJAllowed
	public final void requestTermination() {
		MissionSequencer.terminationRequest = true;
		// It is simple polled in the PAEH - that'S easy ;-)
		// But we should also invoke cleanup().
		// That does not work when requestTermination is invoked
		// before startMission()
		// clean.fire();
		System.out.println("Termination request");
	}

	@SCJAllowed
	public final void requestSequenceTermination() {
		requestTermination();
	}

	@SCJAllowed
	public final boolean terminationPending() {
		return MissionSequencer.terminationRequest;
	}

	@SCJAllowed
	public final boolean sequenceTerminationPending() {
		return false;
	}

	@SCJAllowed
	public static Mission getCurrentMission() {
		return MissionSequencer.current_mission;
		//return null;
	}
	
	/**
	 * NOT PART OF SPEC
	 */
	@SCJAllowed(SUPPORT)
	protected abstract Runnable start();

	
}
