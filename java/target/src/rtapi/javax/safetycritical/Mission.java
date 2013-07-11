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

import java.util.Vector;

import javax.realtime.AsyncEventHandler;
import javax.realtime.AsyncLongEventHandler;
import javax.safetycritical.annotate.Allocate;
import javax.safetycritical.annotate.SCJAllowed;
import javax.safetycritical.annotate.Allocate.Area;

import com.jopdesign.sys.Native;

/**
 * 
 * @author martin
 * 
 */
@SCJAllowed
public abstract class Mission {

	// Workaround to avoid illegal reference:
	// Store the address itself (a number) of
	// the structure containing the handler's
	// registerd in this mission.
	int eventHandlersRef;
	boolean hasEventHandlers = false;

	// See above...
	int longEventHandlersRef;
	boolean hasLongEventHandlers = false;

	// See above...
	int managedInterruptRef;
	boolean hasManagedInterrupt = false;

	// To keep track of the state of a mission
	static final int INACTIVE = 0;
	static final int INITIIALIZATION = 1;
	static final int EXECUTION = 2;
	static final int CLEANUP = 3;

	public int phase = INACTIVE;

	// True only for subclasses of CyclicExecutive
	boolean isCyclicExecutive = false;

	static MissionSequencer currentSequencer = null;

	@Allocate({ Area.THIS })
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

		Terminal.getTerminal().writeln("Mission cleanup");

		if (hasEventHandlers) {
			Vector eventHandlers = getHandlers();
			for (int i = 0; i < eventHandlers.size(); i++) {
				((ManagedEventHandler) eventHandlers.elementAt(i)).cleanUp();
			}

			// The vector lives in mission memory. The removeAllElements()
			// method sets all references to handlers to null. The handler
			// objects are collected when the mission finishes (i.e. when
			// mission memory is exited).
			eventHandlers.removeAllElements();

			// This is actually needed only if mission objects live in immortal
			// memory.
			hasEventHandlers = false;
			eventHandlersRef = 0;

		}

		if (hasLongEventHandlers) {
			Vector longEventHandlers = getLongHandlers();
			for (int i = 0; i < longEventHandlers.size(); i++) {
				((ManagedLongEventHandler) longEventHandlers.elementAt(i))
						.cleanUp();
			}

			longEventHandlers.removeAllElements();
			longEventHandlersRef = 0;
			hasLongEventHandlers = false;

		}

		Vector managedInterrupt = getInterrupts();
		if (managedInterrupt != null) {
			for (int i = 0; i < managedInterrupt.size(); i++) {
				((ManagedInterruptServiceRoutine) managedInterrupt.elementAt(i))
						.unregister();
			}
			
			managedInterrupt.removeAllElements();
			managedInterruptRef = 0;
			hasManagedInterrupt = false;
		}
	}

	@SCJAllowed
	public final void requestTermination() {
		MissionSequencer.terminationRequest = true;
		// It is simple polled in the PAEH - that'S easy ;-)
		// But we should also invoke cleanup().
		// That does not work when requestTermination is invoked
		// before startMission()
		// clean.fire();
		// System.out.println("Termination request");
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
		return currentSequencer.current_mission;
		// return MissionSequencer.current_mission;
		// return null;
	}

	/**
	 * NOT PART OF SPEC, implementation specific
	 */
	Vector getHandlers() {
		return (Vector) Native.toObject(eventHandlersRef);
	}

	Vector getLongHandlers() {
		return (Vector) Native.toObject(longEventHandlersRef);
	}

	public Vector getInterrupts() {
		if (hasManagedInterrupt) {
			return (Vector) Native.toObject(managedInterruptRef);
		} else {
			return null;
		}
		
	}

	// @SCJAllowed(SUPPORT)
	// protected abstract Runnable start();

}
