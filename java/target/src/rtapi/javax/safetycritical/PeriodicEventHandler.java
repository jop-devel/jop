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
import static javax.safetycritical.annotate.Level.LEVEL_0;

import javax.realtime.PriorityParameters;
import javax.realtime.RelativeTime;
import javax.safetycritical.annotate.MemoryAreaEncloses;
import javax.safetycritical.annotate.SCJAllowed;
import javax.safetycritical.annotate.SCJRestricted;

import javax.safetycritical.*;

import static javax.safetycritical.annotate.Phase.INITIALIZATION;

import joprt.RtThread;

/**
 * The class that represents periodic activity. Should be used as the main
 * vehicle for real-time applications.
 * 
 * Now finally we have a class that the implementation can use.
 * 
 * @author Martin Schoeberl
 * 
 */

@SCJAllowed
public abstract class PeriodicEventHandler extends ManagedEventHandler {

	PriorityParameters priority;
	RelativeTime start, period;
	// ThreadConfiguration tconf;
	String name;

	RtThread thread;

	/**
	 * Constructor to create a periodic event handler.
	 * <p>
	 * Does not perform memory allocation. Does not allow this to escape local
	 * scope. Builds links from this to priority and parameters, so those two
	 * arguments must reside in scopes that enclose this.
	 * <p>
	 * 
	 * @param priority
	 *            specifies the priority parameters for this periodic event
	 *            handler. Must not be null.
	 * 
	 * @param parameters
	 *            specifies the periodic release parameters, in particular the
	 *            start time, period and deadline miss and cost overrun
	 *            handlers. Note that a relative start time is not relative to
	 *            NOW but relative to the point in time when initialization is
	 *            finished and the timers are started. This argument must not be
	 *            null.
	 * 
	 * @param scp
	 *            The scp parameter describes the organization of memory
	 *            dedicated to execution of the underlying thread. (added by MS)
	 * 
	 * @throws IllegalArgumentException
	 *             if priority, parameters.
	 */
	@MemoryAreaEncloses(inner = { "this", "this", "this" }, outer = {
			"priority", "parameters", "scp" })
	@SCJAllowed
	@SCJRestricted(phase = INITIALIZATION)
	public PeriodicEventHandler(PriorityParameters priority,
			PeriodicParameters parameters, StorageParameters scp) {
		this(priority, parameters, scp, "");
	}

	/**
	 * Constructor to create a periodic event handler.
	 * <p>
	 * Does not perform memory allocation. Does not allow this to escape local
	 * scope. Builds links from this to priority, parameters, and name so those
	 * three arguments must reside in scopes that enclose this.
	 * <p>
	 * 
	 * @param priority
	 *            specifies the priority parameters for this periodic event
	 *            handler. Must not be null.
	 *            <p>
	 * @param release
	 *            specifies the periodic release parameters, in particular the
	 *            start time and period. Note that a relative start time is not
	 *            relative to NOW but relative to the point in time when
	 *            initialization is finished and the timers are started. This
	 *            argument must not be null.
	 *            <p>
	 * @param scp
	 *            The scp parameter describes the organization of memory
	 *            dedicated to execution of the underlying thread. (added by MS)
	 *            <p>
	 * @throws IllegalArgumentException
	 *             if priority parameters are null.
	 */
	@MemoryAreaEncloses(inner = { "this", "this", "this", "this" }, outer = {
			"priority", "parameters", "scp", "name" })
	@SCJAllowed(LEVEL_1)
	public PeriodicEventHandler(PriorityParameters priority,
			PeriodicParameters release, StorageParameters scp, String name) {
		// TODO: what are we doing with this Managed thing?
		super(priority, release, scp, name);
		this.priority = priority;

		start = release.start;
		period = release.period;
		// TODO scp
		// this.tconf = tconf;
		this.name = name;

		int p = ((int) period.getMilliseconds()) * 1000
				+ period.getNanoseconds() / 1000;
		int off = ((int) start.getMilliseconds()) * 1000
				+ start.getNanoseconds() / 1000;

		thread = new RtThread(priority.getPriority(), p, off) {

			public void run() {
				// TODO: there is no MissionDiscriptor in the actual SCJ
				while (!MissionDescriptor.terminationRequest) {
					handleAsyncEvent();
					waitForNextPeriod();
				}
			}
		};
	}

	/**
	 * @see javax.safetycritical.ManagedSchedulable#register() Registers this
	 *      event handler with the current mission.
	 */
	@SCJAllowed
	@Override
	@SCJRestricted(phase = INITIALIZATION)
	public final void register() {

	}
}
