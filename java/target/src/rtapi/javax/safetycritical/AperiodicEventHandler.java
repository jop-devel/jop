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

import java.util.Vector;

import javax.realtime.AperiodicParameters;
import javax.realtime.PriorityParameters;
import javax.safetycritical.annotate.MemoryAreaEncloses;
import javax.safetycritical.annotate.SCJAllowed;
import javax.safetycritical.annotate.SCJRestricted;

import com.jopdesign.sys.Memory;
import com.jopdesign.sys.Native;

import joprt.SwEvent;

import static javax.safetycritical.annotate.Phase.INITIALIZATION;

/**
 * @author Martin Schoeberl
 * 
 */
@SCJAllowed(LEVEL_1)
public abstract class AperiodicEventHandler extends ManagedEventHandler {

	String name;
	SwEvent event;
	Memory privMem;

	@MemoryAreaEncloses(inner = { "this", "this", "this", "this" }, outer = {
			"priority", "release_info", "mem_info", "event" })
	@SCJAllowed(LEVEL_1)
	@SCJRestricted(phase = INITIALIZATION)
	public AperiodicEventHandler(PriorityParameters priority,
			AperiodicParameters release, StorageParameters storage, long scopeSize) {
		this(priority, release, storage, scopeSize, "");
	}

	@MemoryAreaEncloses(inner = { "this", "this", "this", "this", "this" }, outer = {
			"priority", "release_info", "scp", "event", "name" })
	@SCJAllowed(LEVEL_1)
	@SCJRestricted(phase = INITIALIZATION)
	public AperiodicEventHandler(PriorityParameters priority,
			AperiodicParameters release, StorageParameters storage, long scopeSize, String name) {
		super(priority, release, storage, name);

		if (storage != null) {
			privMem = new Memory((int) scopeSize, (int) storage.getTotalBackingStoreSize());
		}

		final Runnable runner = new Runnable() {
			@Override
			public void run() {
				handleAsyncEvent();
			}
		};

		this.name = name;

		// Aperiodic = Sporadic with minimum inter-arrival time set to zero
		event = new SwEvent(priority.getPriority(), 0) {

			@Override
			public void handle() {
				privMem.enter(runner);
			}

		};

	}

	// @MemoryAreaEncloses(inner = { "this", "this", "this", "this" }, outer = {
	// "priority", "release_info", "scp", "events" })
	// @SCJAllowed(LEVEL_1)
	// @SCJRestricted(phase = INITIALIZATION)
	// public AperiodicEventHandler(PriorityParameters priority,
	// AperiodicParameters release, StorageParameters scp,
	// AperiodicEvent[] events) {
	// super(null, null, null, null);
	// }
	//
	// @MemoryAreaEncloses(inner = { "this", "this", "this", "this", "this" },
	// outer = {
	// "priority", "release_info", "scp", "events", "name" })
	// @SCJAllowed(LEVEL_1)
	// @SCJRestricted(phase = INITIALIZATION)
	// public AperiodicEventHandler(PriorityParameters priority,
	// AperiodicParameters release, StorageParameters scp,
	// AperiodicEvent[] events, String name) {
	// super(null, null, null, null);
	// }

	@SCJAllowed
	@SCJRestricted(phase = INITIALIZATION)
	public final void register() {
		Mission m = Mission.getCurrentMission();
		if (!m.hasEventHandlers){
//			System.out.println("creating MEH vector...");
			m.eventHandlersRef = Native.toInt(new Vector());
			m.hasEventHandlers = true;
		}
		
		((Vector) Native.toObject(m.eventHandlersRef)).addElement(this);
	}

	/**
	 * This method is concrete in the RTSJ superclass, but now it is abstract.
	 */
	public abstract void handleAsyncEvent();

	/**
	 * An internal method to unblock the handler.
	 */
	void unblock() {
		// TODO Auto-generated method stub
	}

	public final void release() {
		event.fire();
	}
}
