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

import java.util.Vector;

import javax.realtime.AbsoluteTime;
import javax.realtime.HighResolutionTime;
import javax.realtime.PeriodicParameters;
import javax.realtime.PriorityParameters;
import javax.realtime.RelativeTime;
import javax.safetycritical.annotate.MemoryAreaEncloses;
import javax.safetycritical.annotate.SCJAllowed;
import javax.safetycritical.annotate.SCJRestricted;

import com.jopdesign.sys.Memory;
import com.jopdesign.sys.Native;

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
	StorageParameters storage;
	// ThreadConfiguration tconf;
	String name;
	Mission m;
	Memory privMem;

	RtThread thread;
	
	long scopeSize;

	@MemoryAreaEncloses(inner = { "this", "this", "this" }, outer = {
			"priority", "parameters", "scp" })
	@SCJAllowed
	@SCJRestricted(phase = INITIALIZATION)
	public PeriodicEventHandler(PriorityParameters priority,
			PeriodicParameters release, StorageParameters storage, long scopeSize) {
		this(priority, release, storage, scopeSize, "");
	}

	@MemoryAreaEncloses(inner = { "this", "this", "this", "this" }, outer = {
			"priority", "parameters", "scp", "name" })
	@SCJAllowed(LEVEL_1)
	public PeriodicEventHandler(PriorityParameters priority,
			PeriodicParameters release, StorageParameters storage, long scopeSize,
			String name) {
		// TODO: what are we doing with this Managed thing?
		super(priority, release, storage, name);
		
		this.priority = priority;
		this.storage = storage;
		this.scopeSize = scopeSize;

		start = (RelativeTime) release.getStart();
		period = release.getPeriod();
		// TODO scp
		// this.tconf = tconf;
		this.name = name;
		int p = ((int) period.getMilliseconds()) * 1000
				+ period.getNanoseconds() / 1000;
		if (p < 0) { // Overflow
			p = Integer.MAX_VALUE;
		}
		int off = ((int) start.getMilliseconds()) * 1000
				+ start.getNanoseconds() / 1000;
		if (off < 0) { // Overflow
			off = Integer.MAX_VALUE;
		}

		m = Mission.getCurrentMission();

		if (storage != null) {
			// Create handler's private memory, except for cyclic executives, where
			// a single private memory is reused for all handlers.
			// Mission should not be null at this point, as PEH's are created at
			// mission initialization.
			if (!m.isCyclicExecutive) {
				
				privMem = new Memory((int) scopeSize, (int) storage.getTotalBackingStoreSize());
				
			}
		}

		// No need to create this runnable or a RT thread for cyclic executives
		// where handler's handleAsyncEvent method is called directly. 
		if (!m.isCyclicExecutive) {
			final Runnable runner = new Runnable() {
				@Override
				public void run() {
					handleAsyncEvent();
				}
			};

			thread = new RtThread(priority.getPriority(), p, off) {

				public void run() {
					while (!MissionSequencer.terminationRequest) {
						privMem.enter(runner);
						waitForNextPeriod();
					}
				}

			};
		}
	}

	@SCJAllowed
	@SCJRestricted(phase = INITIALIZATION)
	public final void register() {
		
		final Mission m = Mission.getCurrentMission();
		
		if (!m.hasEventHandlers){
//			System.out.println("creating MEH vector...");
			m.eventHandlersRef = Native.toInt(new Vector());
			m.hasEventHandlers = true;
		}
		
		((Vector) Native.toObject(m.eventHandlersRef)).addElement(this);
		
		
	}

	/**
	 * Get the actual start time of this handler. The actual start time of the
	 * handler is different from the requested start time (passed at construction
	 * time) when the requested start time is an absolute time that would occur 
	 * before the mission has been started. In this case, the actual start time 
	 * is the time the mission started. If the actual start time is equal to the 
	 * effect start time, then the method behaves as if getResquestedStartTime() 
	 * method has been called. If it is different, then a newly created time object 
	 * is returned. The time value is associated with the same clock as that used 
	 * with the original start time parameter.
	 * 
	 * @return a reference to a time parameter based on the clock used to start
	 *         the timer.
	 */
	@SCJAllowed(LEVEL_1)
	public HighResolutionTime getActualStartTime() {
		return null;

	}

	/**
	 * Get the effective start time of this handler. If the clock associated
	 * with the start time parameter and the interval parameter (that were
	 * passed at construction time) are the same, then the method behaves as if
	 * getActualStartTime() has been called. If the two clocks are different,
	 * then the method returns a newly created object whose time is the current
	 * time of the clock associated with the interval parameter (passed at
	 * construction time) when the handler is actually started.
	 * 
	 * @return a reference based on the clock associated with the interval
	 *         parameter.
	 */
	@SCJAllowed(LEVEL_1)
	public HighResolutionTime getEffectiveStartTime() {
		return null;

	}

	/**
	 * Get the last release time of this handler.
	 * 
	 * @return a reference to a newly-created javax.safetycritical.AbsoluteTime
	 *         object representing this handlers’s last release time, according
	 *         to the clock associated with the interval parameter used at
	 *         construction time.
	 * @throws IllegalStateException
	 *             if this timer has not been released since it was last
	 *             started.
	 */
	@SCJAllowed(LEVEL_1)
	public AbsoluteTime getLastReleaseTime() throws IllegalStateException {
		return null;

	}

	/**
	 * Get the time at which this handler is next expected to be released.
	 * 
	 * 
	 * @return The absolute time at which this handler is expected to be
	 *         released in a newly allocated javax.safetycritical.AbsoluteTime
	 *         object. The clock association of the returned time is the clock
	 *         on which interval parameter (passed at construction time) is
	 *         based.
	 * 
	 * @throws ArithmeticException
	 *             if the result does not fit in the normalized format. Throws
	 *             IllegalStateException Thrown if this handler has not been
	 *             started.
	 */
	@SCJAllowed(LEVEL_1)
	public AbsoluteTime getNextReleaseTime() throws ArithmeticException {
		return null;
	}

	/**
	 * Get the requested start time of this periodic handler. Note that the
	 * start time uses copy semantics, so changes made to the value returned by
	 * this method will not effect the requested start time of this handler if
	 * it has not already been started.
	 * 
	 * @return a reference to the start time parameter in the release parameters
	 *         used when constructing this handler.
	 */
	@SCJAllowed(LEVEL_1)
	public HighResolutionTime getRequestedStartTime() {
		return null;
	}

	/**
	 * Get the time at which this handler is next expected to be released.
	 * 
	 * @param dest
	 *            The instance of javax.safetycritical.AbsoluteTime which will
	 *            be updated in place and returned. The clock association of the
	 *            dest parameter is ignored. When dest is null a new object is
	 *            allocated for the result.
	 * 
	 * @return the instance of javax.safetycritical.AbsoluteTime passed as
	 *         parameter, with time values representing the absolute time at
	 *         which this handler is expected to be released. If the dest
	 *         parameter is null the result is returned in a newly allocated
	 *         object. The clock association of the returned time is the clock
	 *         on which the interval parameter (passed at construction time) is
	 *         based.
	 * 
	 * @throws ArithmeticException
	 *             If the result does not fit in the normalized format.
	 * 
	 * @throws IllegalStateException
	 *             If this handler has not been started.
	 */
	@SCJAllowed(LEVEL_1)
	public AbsoluteTime getnextReleaseTime(AbsoluteTime dest)
			throws ArithmeticException, IllegalStateException {
		return null;
	}
	
	/**
	 * Not on spec, implementation specific
	 */
	long getScopeSize(){
		return this.scopeSize;
	}
}
