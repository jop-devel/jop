/*
  This file is part of JOP, the Java Optimized Processor
    see <http://www.jopdesign.com/>
  This subset of javax.realtime is provided for the JSR 302
  Safety Critical Specification for Java

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
 
  @authors  Martin Schoeberl, Lei Zhao, Ales Plsek, Tórur Strøm
 */

package javax.realtime;

import javax.safetycritical.annotate.Allocate;
import javax.safetycritical.annotate.BlockFree;
import javax.safetycritical.annotate.MemoryAreaEncloses;
import javax.safetycritical.annotate.SCJAllowed;
import javax.safetycritical.annotate.SCJProtected;
import javax.safetycritical.annotate.SCJRestricted;

import static javax.safetycritical.annotate.Allocate.Area.CURRENT;

/**
 * An object that represents a specific point in time given by milliseconds plus
 * nanoseconds past some point in time fixed by the clock. For the default
 * real-time clock the fixed point is the implementation dependent Epoch. The
 * correctness of the Epoch as a time base depends on the real-time clock
 * synchronization with an external world time reference.
 * 
 * A time object in normalized form represents negative time if both components
 * are nonzero and negative, or one is nonzero and negative and the other is
 * zero. For add and subtract negative values behave as they do in arithmetic.
 */
@SCJAllowed
public class AbsoluteTime extends HighResolutionTime {

	/**
	 * Construct an AbsoluteTime object with time millisecond and nanosecond
	 * components past the real-time clock's Epoch.
	 * 
	 * @param ms
	 *            The desired value for the millisecond component of this. The
	 *            actual value is the result of parameter normalization.
	 * @param ns
	 *            The desired value for the nanosecond component of this. The
	 *            actual value is the result of parameter normalization.
	 */
	@BlockFree
	@SCJAllowed
	@SCJRestricted(maySelfSuspend = false)
	public AbsoluteTime(long millis, int nanos) {
		super(millis, nanos);
	}

	/**
	 * Equivalent to new AbsoluteTime(0,0).
	 */
	@SCJAllowed
	@SCJRestricted(maySelfSuspend = false)
	public AbsoluteTime() {
		this(0,0);
	}

	// /**
	// * TBD: Do we want to require MemoryAreaEncloses(inner = {"this"}, outer =
	// * {"time.getClock()"})? PERC Pico says that Clock must be allocated in
	// * immortal memory to avoid this "difficulty"?
	// */
	/**
	 * Make a new AbsoluteTime object from the given AbsoluteTime object.
	 * 
	 * @param The
	 *            AbsoluteTime object which is the source for the copy.
	 */
	@SCJAllowed
	@SCJRestricted(maySelfSuspend = false)
	public AbsoluteTime(AbsoluteTime time) {
		super(time);
	}

	/**
	 * Construct an AbsoluteTime object with time millisecond and nanosecond
	 * components past the epoch for clock.
	 * 
	 * @param ms
	 *            The desired value for the millisecond component of this. The
	 *            actual value is the result of parameter normalization.
	 * @param ns
	 *            The desired value for the nanosecond component of this. The
	 *            actual value is the result of parameter normalization.
	 * @param clock
	 *            The clock providing the association for the newly constructed
	 *            object.
	 */
	@SCJAllowed
	@SCJRestricted(maySelfSuspend = false)
	public AbsoluteTime(long millis, int nanos, Clock clock) {
		super(millis, nanos, clock);
	}

	/**
	 * Equivalent to new AbsoluteTime(0,0,clock).
	 * 
	 * @param clock
	 *            The clock providing the association for the newly constructed
	 *            object.
	 */
	@SCJAllowed
	@SCJRestricted(maySelfSuspend = false)
	public AbsoluteTime(Clock clock) {
		this(0, 0, clock);
	}

	/**
	 * Return an object containing the value resulting from adding millis and
	 * nanos to the values from this and normalizing the result.
	 * 
	 * @param millis
	 *            The number of milliseconds to be added to this.
	 * @param nanos
	 *            The number of nanoseconds to be added to this.
	 * @param dest
	 *            If dest is not null, the result is placed there and returned.
	 *            Otherwise, a new object is allocated for the result.
	 * @return the result of the normalization of this plus millis and nanos in
	 *         dest if dest is not null, otherwise the result is returned in a
	 *         newly allocated object.
	 */
	@SCJAllowed
	@SCJRestricted(maySelfSuspend = false)
	public AbsoluteTime add(long millis, int nanos, AbsoluteTime dest) {
		return (AbsoluteTime) super.add(millis, nanos,
				dest == null ? new AbsoluteTime(0, 0, clock) : dest);
	}
	
	/**
	 * Return an object containing the value resulting from adding time to the
	 * value of this and normalizing the result.
	 * 
	 * @param time
	 *            The time to add to this.
	 * @param dest
	 *            If dest is not null, the result is placed there and returned.
	 *            Otherwise, a new object is allocated for the result.
	 * @return the result of the normalization of this plus the RelativeTime
	 *         parameter time in dest if dest is not null, otherwise the result
	 *         is returned in a newly allocated object.
	 */
	@SCJAllowed
	@SCJRestricted(maySelfSuspend = false)
	public AbsoluteTime add(RelativeTime time, AbsoluteTime dest) {
		if (time == null || time.clock != clock)
			throw new IllegalArgumentException("null arg or different clock");

		return add(time.millis, time.nanos, dest);
	}
	
	/**
	 * Create a new instance of AbsoluteTime representing the result of adding
	 * time to the value of this and normalizing the result.
	 * 
	 * @param time
	 *            The time to add to this.
	 * @return A new AbsoluteTime object whose time is the normalization of this
	 *         plus the parameter time.
	 */
	@Allocate( { CURRENT })
	@SCJAllowed
	@SCJRestricted(maySelfSuspend = false)
	public AbsoluteTime add(RelativeTime time) {
		
		return add(time,null);
	}
	
	/**
	 * Create a new object representing the result of adding millis and nanos to
	 * the values from this and normalizing the result.
	 * 
	 * @param millis
	 *            The number of milliseconds to be added to this.
	 * @param nanos
	 *            The number of nanoseconds to be added to this.
	 * @return A new AbsoluteTime object whose time is the normalization of this
	 *         plus millis and nanos.
	 */
	@Allocate( { CURRENT })
	@SCJAllowed
	@SCJRestricted(maySelfSuspend = false)
	public AbsoluteTime add(long millis, int nanos) {
		return add(millis, nanos, null);
	}
	
	/**
	 * Return an object containing the value resulting from subtracting time
	 * from the value of this and normalizing the result.
	 * 
	 * @param time
	 *            The time to subtract from this.
	 * @param dest
	 *            If dest is not null, the result is placed there and returned.
	 *            Otherwise, a new object is allocated for the result.
	 * @return the result of the normalization of this minus the AbsoluteTime
	 *         parameter time in dest if dest is not null, otherwise the result
	 *         is returned in a newly allocated object.
	 */
	@SCJAllowed
	@SCJRestricted(maySelfSuspend = false)
	public RelativeTime subtract(AbsoluteTime time, RelativeTime dest) {
		if (time == null || time.clock != this.clock)
			throw new IllegalArgumentException("null arg or different clock");

		if (dest == null)
			dest = new RelativeTime(0, 0, clock);

		return (RelativeTime) add(-time.millis, -time.nanos, dest);
	}
	
	/**
	 * Return an object containing the value resulting from subtracting time
	 * from the value of this and normalizing the result.
	 * 
	 * @param time
	 *            The time to subtract from this.
	 * @param dest
	 *            If dest is not null, the result is placed there and returned.
	 *            Otherwise, a new object is allocated for the result.
	 * @return the result of the normalization of this minus the RelativeTime
	 *         parameter time in dest if dest is not null, otherwise the result
	 *         is returned in a newly allocated object.
	 */
	@SCJAllowed
	@SCJRestricted(maySelfSuspend = false)
	public AbsoluteTime subtract(RelativeTime time, AbsoluteTime dest) {
		if (time == null || time.clock != clock)
			throw new IllegalArgumentException("null arg or different clock");

		return add(-time.millis, -time.nanos, dest);
	}
	
	/**
	 * Create a new instance of AbsoluteTime representing the result of
	 * subtracting time from the value of this and normalizing the result.
	 * 
	 * @param time
	 *            The time to subtract from this.
	 * @return A new AbsoluteTime object whose time is the normalization of this
	 *         minus the parameter time.
	 */
	@Allocate( { CURRENT })
	@SCJAllowed
	@SCJRestricted(maySelfSuspend = false)
	public AbsoluteTime subtract(RelativeTime time) {
		return subtract(time,null);
	}

	/**
	 * Create a new instance of RelativeTime representing the result of
	 * subtracting time from the value of this and normalizing the result.
	 * 
	 * @param time
	 *            The time to subtract from this.
	 * @return A new RelativeTime object whose time is the normalization of this
	 *         minus the AbsoluteTime parameter time.
	 */
	@Allocate( { CURRENT })
	@SCJAllowed
	@SCJRestricted(maySelfSuspend = false)
	public RelativeTime subtract(AbsoluteTime time) {
		return subtract(time, null);
	}
}
