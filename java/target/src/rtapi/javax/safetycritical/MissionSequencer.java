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

import static javax.safetycritical.annotate.Level.LEVEL_2;
import static javax.safetycritical.annotate.Level.SUPPORT;
import static javax.safetycritical.annotate.Level.INFRASTRUCTURE;

import javax.realtime.BoundAsyncEventHandler;
import javax.realtime.PriorityParameters;
import javax.safetycritical.annotate.MemoryAreaEncloses;
import javax.safetycritical.annotate.SCJAllowed;
import javax.safetycritical.annotate.SCJRestricted;

import com.jopdesign.sys.RtThreadImpl;

import joprt.RtThread;
import joprt.SwEvent;

import static javax.safetycritical.annotate.Phase.INITIALIZATION;

/**
 * That's the root of (all evil ;-), no the main startup logic...
 * 
 * @author Martin Schoeberl
 * 
 * @param <SpecificMission>
 */
@SCJAllowed
public abstract class MissionSequencer<SpecificMission extends Mission> extends
		ManagedEventHandler {

	private SwEvent clean;
//	private boolean cleanupDidRun;
	public static boolean cleanupDidRun;
	
	Mission current_mission;

	// why is this static?
	// ok, in level 1 we have only one mission.
	// But it is ugly that Mission does not know about its
	// sequencer and therefore cannot call the termination on
	// a specific one.
	static boolean terminationRequest = false;

	/**
	 * The constructor just sets the initial state.
	 * 
	 * @param priority
	 * @param storage
	 * @param name
	 */
	@SCJAllowed
	@MemoryAreaEncloses(inner = { "this", "this", "this" }, outer = {
			"priority", "storage", "name" })
	@SCJRestricted(phase = INITIALIZATION)
	public MissionSequencer(PriorityParameters priority,
			StorageParameters storage, String name) {
		// MS: just to make the system happy, but we don't want to
		// really extend the handler.... We want to run in the
		// plain Java thread!
		// in Level 1 we can simply ignore the priority
		super(priority, null, storage, name);
		// just an idle thread that watches the termination request
		// We should use the inital main thread to watch for termination...

		new RtThread(0, 10000) {
			public void run() {
				while (!MissionSequencer.terminationRequest) {
					waitForNextPeriod();
				}
				// Why do we need to call the cleanUp() method of the next
				// mission after a termination request in the current mission?
				//getNextMission().cleanUp();
				
				// Current mission cleanup method
				current_mission.cleanUp();
				
				// MEH cleanUp method 
				cleanUp();
				
				cleanupDidRun = true;
				
			}
			
		};
		
//		final Runnable runner = new Runnable() {
//			
//			public void run() {
//				handleAsyncEvent();
//				
//			}
//		};
//		
//		new RtThread(0, 10000){
//
//			public void run() {
//				while (!MissionSequencer.terminationRequest) {
//
//					// This should be the mission memory
//					privMem.enter(runner);
//					block();
////					waitForNextPeriod();
//				}
//				// Current mission cleanup method
//				current_mission.cleanUp();
//				
//				// MEH cleanUp method 
//				cleanUp();
//			}
//		};
		
		
		
		// clean = new SwEvent(0, 100) {
		// public void handle() {
		// if (!cleanupDidRun && terminationRequest) {
		// cleanup();
		// }
		// }
		// };
	}

	@SCJAllowed
	@MemoryAreaEncloses(inner = { "this" }, outer = { "priority" })
	@SCJRestricted(phase = INITIALIZATION)
	public MissionSequencer(PriorityParameters priority,
			StorageParameters storage) {
		this(priority, storage, "");
	}

	@SCJAllowed(SUPPORT)
	protected abstract SpecificMission getNextMission();

	/**
	 * Inherited because we extend MEH although we could use composition....
	 */
	@Override
	@SCJAllowed(INFRASTRUCTURE)
	public final void handleAsyncEvent() {
		
//		current_mission.setSequencer(this);
		
//		System.out.println("getting new mission");
//		
////		if (current_mission.phase == Mission.INACTIVE){
//			// Currently used as a means to start the single mission from another package
//			Mission mission = getNextMission();
//			
//			mission.phase = Mission.INITIALIZATION;
//			
//			// TODO: The spec mentions something about mission memory resize...
//			// mission.missionMemorySize();
//			
//			mission.initialize();
//		}
		
	}

	/**
	 * Inherited because we extend MEH although we could use composition....
	 */
	@SCJAllowed
	@SCJRestricted(phase = INITIALIZATION)
	public final void register() {
		// NOOP in Level 1
	}

	@SCJAllowed(LEVEL_2)
	public final void requestSequenceTermination() {
		// Why do we need to call the cleanUp() method of the next
		// mission after a termination request in the current mission?
		//this.getNextMission().requestTermination();
		current_mission.requestTermination();
	}

	@SCJAllowed(LEVEL_2)
	public final boolean sequenceTerminationPending() {
		return false;
	}
}
