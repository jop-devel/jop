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

import static javax.safetycritical.annotate.Level.LEVEL_2;

import javax.safetycritical.annotate.SCJAllowed;
import javax.safetycritical.annotate.SCJRestricted;

/**
 * Class HighResolutionTime is the base class for AbsoluteTime, RelativeTime,
 * RationalTime. Used to express time with nanosecond accuracy. This class is
 * never used directly: it is abstract and has no public constructor. Instead,
 * one of its subclasses AbsoluteTime, RelativeTime, or RationalTime should be
 * used.
 * 
 */
@SCJAllowed
public abstract class HighResolutionTime { // implements Comparable {

	static final int NANOS_PER_MILLI = 1000 * 1000;
	
	/**
	 * milliseconds part of the time.
	 */
	long millis;
	/**
	 * nanoseconds part of the time.
	 */
	int nanos;

	/**
	 * the clock associated with this time.
	 * This is only interesting when user-defined clocks are
	 * used, which are a Level 2 feature.
	 */
	Clock clock;
	
	HighResolutionTime(long millis, int nanos) {
		clock = Clock.getRealtimeClock();
		set(millis, nanos);
	}
	
	HighResolutionTime(HighResolutionTime time) {
		millis = time.millis;
		nanos = time.nanos;
		clock = time.clock;
	}
	
	HighResolutionTime(long millis, int nanos, Clock clock) {
		this.clock = clock;
		set(millis, nanos);
	}
	
	/**
	 * Compares this HighResolutionTime with the specified HighResolutionTime
	 * time.
	 * 
	 * @param time
	 *            Compares with the time of this.
	 * @return
	 */
	@SCJAllowed
	@SCJRestricted(maySelfSuspend = false)
	public int compareTo(HighResolutionTime time) {
        if (time == null)
            throw new IllegalArgumentException("null parameter");
        // We are missing reflection in JOP - T�rur 3/6/2011
        /*if (getClass() != time.getClass())
            throw new ClassCastException();*/
        if (clock != time.clock)
            throw new IllegalArgumentException("different clocks");
        if (millis > time.millis)
            return 1;
        else if (millis < time.millis)
            return -1;
        else
            return nanos - time.nanos;
	}

	/**
	 * Compares this HighResolutionTime with the specified object.
	 * 
	 * @param object
	 *            Compares with the time of this.
	 * @return
	 */
	@SCJAllowed
	@SCJRestricted(maySelfSuspend = false)
	public int compareTo(java.lang.Object object) {
		return compareTo((HighResolutionTime) object);
	}
	
	/**
	 * Returns true if the argument object has the same type and values as this.
	 * 
	 * @param time
	 *            Value compared to this.
	 * @return true if the parameter object is of the same type and has the same
	 *         values as this.
	 */
	@SCJAllowed
	@SCJRestricted(maySelfSuspend = false)
	public boolean equals(HighResolutionTime time) {
		return (millis == time.millis && nanos == time.nanos);
	}

	/**
	 * Returns true if the argument object has the same type and values as this.
	 * 
	 * @param object
	 *            Value compared to this.
	 * @return true if the parameter object is of the same type and has the same
	 *         values as this.
	 */
	@SCJAllowed
	@SCJRestricted(maySelfSuspend = false)
	public boolean equals(java.lang.Object object) {
		return equals((HighResolutionTime)object);
	}
	
	/**
	 * At the moment just return the single real-time clock.
	 * 
	 * @return A reference to the clock associated with this.
	 */
	@SCJAllowed
	@SCJRestricted(maySelfSuspend = false)
	public Clock getClock() {
		return clock;
	}
	
	/**
	 * 
	 * @return The milliseconds component of the time represented by this.
	 */
	@SCJAllowed
	@SCJRestricted(maySelfSuspend = false)
	public final long getMilliseconds() {
		return millis;
	}

	/**
	 * 
	 * @return The nanoseconds component of the time represented by this.
	 */
	@SCJAllowed
	@SCJRestricted(maySelfSuspend = false)
	public final int getNanoseconds() {
		return nanos;
	}

	/**
	 * Returns a hash code for this object in accordance with the general
	 * contract of Object.hashCode().
	 * 
	 * @return The hashcode value for this instance.
	 */
	@SCJAllowed
	@SCJRestricted(maySelfSuspend = false)
	public int hashCode() {
		// TODO lookup the chapter how to best generate a hash code.
		return (int) millis + nanos;
	}
	
	/**
	 * Sets the millisecond component of this to the given argument, and the
	 * nanosecond component of this to 0.
	 * 
	 * @param millis
	 *            This value shall be the value of the millisecond component of
	 *            this at the completion of the call.
	 */
	@SCJAllowed
	@SCJRestricted(maySelfSuspend = false)
	public void set(long millis) {
		set(millis, 0);
	}
	
	/**
	 * Sets the millisecond and nanosecond components of this.
	 * 
	 * @param millis
	 *            The desired value for the millisecond component of this at the
	 *            completion of the call. The actual value is the result of
	 *            parameter normalization.
	 * @param nanos
	 *            The desired value for the nanosecond component of this at the
	 *            completion of the call. The actual value is the result of
	 *            parameter normalization.
	 */
	@SCJAllowed
	@SCJRestricted(maySelfSuspend = false)
	public void set(long millis, long nanos) {
		final long millis_in_nanos = nanos / NANOS_PER_MILLI;
        final int nanosleft = (int) (nanos % NANOS_PER_MILLI);
        if (millis > 0) {
            if (nanos < 0) { // no overflow possible
                this.millis = millis + millis_in_nanos;
                // ensure same sign
                if (this.millis > 0 && nanosleft != 0) {
                    this.millis--;
                    this.nanos = nanosleft + NANOS_PER_MILLI;
                } else {
                    this.nanos = nanosleft;
                }
            } else { // watch for overflow
                long tmp = millis + millis_in_nanos;
                if (tmp <= 0) {
                    // What should we do in case of overflow? - Tórur 3/6/2011
                	throw new ArithmeticException("overflow");
                	//return false
                }
                this.millis = tmp;
                this.nanos = nanosleft;
            }
        } else if (millis < 0) {
            if (nanos < 0) { // watch for negative overflow
                long tmp = millis + millis_in_nanos;
                if (tmp >= 0) {
                	// What should we do in case of overflow? - Tórur 3/6/2011
                	throw new ArithmeticException("overflow");
                	//return false
                }
                this.millis = tmp;
                this.nanos = nanosleft;
            } else { // no overflow possible
                this.millis = millis + millis_in_nanos;
                // ensure same sign
                if (this.millis < 0 && nanosleft != 0) {
                    this.millis++;
                    this.nanos = nanosleft - NANOS_PER_MILLI;
                } else {
                    this.nanos = nanosleft;
                }
            }
        } else { // millis == 0
            this.millis = millis_in_nanos;
            this.nanos = nanosleft;
        }
	}
	
	/**
	 * Change the value represented by this to that of the given time.
	 * 
	 * @param time
	 *            The new value for this.
	 */
	@SCJAllowed
	@SCJRestricted(maySelfSuspend = false)
	public void set(HighResolutionTime time) {
		millis = time.millis;
		nanos = time.nanos;
	}

//	/**
//	 * Behaves exactly like target.wait() but with the enhancement that it waits
//	 * with a precision of HighResolutionTime.
//	 * 
//	 * @param target
//	 *            The object on which to wait. The current thread must have a
//	 *            lock on the object.
//	 * @param time
//	 *            The time for which to wait. If it is RelativeTime(0,0) then
//	 *            wait indefinitely. If it is null then wait indefinitely.
//	 * @throws java.lang.InterruptedException
//	 */
//	@SCJAllowed(LEVEL_2)
//    public static void waitForObject(java.lang.Object target,
//            HighResolutionTime time) throws java.lang.InterruptedException {
//        if (target == null)
//            throw new NullPointerException("null target");
//
//        if (time != null) {
//            if (time.clock != Clock.single)
//                throw new UnsupportedOperationException("Incompatible clock");
//
//            if (time instanceof AbsoluteTime) {
//                time = ((AbsoluteTime) time).subtract(Clock.single.getTime());
//                //target.wait(time.getMilliseconds(), time.getNanoseconds());
//            } else {
//                /*if (time.isNegative())
//                    throw new IllegalArgumentException("negative relative time");
//                else
//                    target.wait(time.getMilliseconds(), time.getNanoseconds());*/
//            }
//        } else
//            target.wait();
//    }
	
	HighResolutionTime add(long millis, int nanos, HighResolutionTime dest) {
//        if (!
        dest.set(addSafe(this.millis, millis), ((long) this.nanos) + nanos);
//        )
//            throw new ArithmeticException("non-normalizable result");
//        dest.setClock(_clock);
        return dest;
    }
	
	/**
     * Adds the two given values together, returning their sum if there is no
     * overflow.
     */
    static long addSafe(long arg1, long arg2) {
        long sum = arg1 + arg2;
            if ((arg1 > 0 && arg2 > 0 && sum <= 0)
                    || (arg1 < 0 && arg2 < 0 && sum >= 0))
                throw new ArithmeticException("overflow");

        return sum;
    }

}
