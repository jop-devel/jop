/*
 * -----------------------------------------------------------------
 * RelativeTime.java :
 *
 * Copyright (C) 2001 TimeSys Corporation, All Rights Reserved.
 *
 * This software is subject to the terms and conditions of the
 * accompanying Common Public License in file CPLicense.  Your use
 * of this software indicates your acceptance of these terms.
 * -----------------------------------------------------------------
 */

package javax.realtime;
// import java.util.Date;

/**
 * An object that represents a time interval millis/1E3+nanos/1E9 seconds long. It
 * generally is used to represent a time relative to now.
 * Caution: This class is explicitly unsafe in multithreaded situations when it is
 * being changed. No synchronization is done. It is assumed that users of this class who
 * are mutating instances will be doing their own synchronization at a higher level.
 *
 * All Implemented Interfaces: java.lang.Comparable
 * Direct Known Subclasses: RationalTime
 */


public class RelativeTime extends HighResolutionTime
{

	/**
	* Equivalent to new RelativeTime(0,0)
	*/
	public RelativeTime(){
			
		this(0,0);
	}

	/**
	* Construct a RelativeTime object which means a time millis milliseconds
	* plus nanos nanoseconds  past the Clock time.
	* @param millis The milliseconds component of the time past the Clock time
	* @param nanos  The nanoseconds component of the time past the Clock time
	*/
	public RelativeTime(long millis, int nanos){
		this.milliseconds = (int) millis;
		this.nanoseconds = nanos;
	}

	/**	
	* Make a new RelativeTime object from the given RelativeTime object
	* @param time  The RelativeTime object used as the source for the copy
	*/
	public RelativeTime(RelativeTime time){
	    if(time == null)
		; //throw new IllegalArgumentException("Illegal null argument");
	    
	    this.milliseconds = (int) time.getMilliseconds();
	    this.nanoseconds = time.getNanoseconds();
	}

	/** Convert this time to an absolute time. For a RelativeTime, this invovled
	* adding the clocks notion of now to this interval and constructing a new
	* AbsoluteTime based on the sum
	*
	* @param clock if null, Clock.getRealTimeClock() is used
	* @param destination
	*/
	/*
	public AbsoluteTime absolute(Clock clock, AbsoluteTime destination){
		AbsoluteTime result;

		if(clock==null)
			clock = Clock.getRealtimeClock();
		if(destination==null){
			destination = clock.getTime();
		}
		else{
			clock.getTime(destination);
		}

		result = destination.add(milliseconds,nanoseconds,destination);

		return result;
	}
	*/


	/*
    public AbsoluteTime absolute(Clock clock){
	AbsoluteTime result;
	AbsoluteTime destination = null;
	if(clock==null){
	    clock = Clock.getRealtimeClock();
	}
	destination = clock.getTime();
	result = destination.add(milliseconds,nanoseconds,destination);
	return result;
    }
	

    public RelativeTime relative(Clock clock){
	return this;
    }    

    public RelativeTime relative(Clock clock, RelativeTime destination){    
	if(destination != null){
	    destination.set(milliseconds, nanoseconds);
	}
	return this;
    }
    */    
    
    /**
     * Add a specific number of milli and nano seconds to <code>this</code>.
     * A new object is allocated
     *
     * @param millis	milli seconds to add
     * @param nanos	nano seconds to add
     * @return A new object containing the result
     */
    public RelativeTime add(long millis, int nanos){
	
	long total = getTotalNanos()+millis*1000000+nanos;
	setTotalNanos(total);
	
	return new RelativeTime(millisecondsTmp,nanosecondsTmp);
    }	

	/**
	* Add a specific number of milli and nano seconds to <code>this</code>.
	* A new object is allocated if destination is null, otherwise store there.
	*
	* @param millis	milli seconds to add
	* @param nanos	nano seconds to add
	* @param destination	to store the result
	* @return A new object containing the result
	*/
	public RelativeTime add(long millis, int nanos, RelativeTime destination){

		long total = getTotalNanos()+millis*1000000+nanos;
		setTotalNanos(total);
	
		if(destination!=null){
			destination.set(millisecondsTmp,nanosecondsTmp);
			return destination;
		}

		return new RelativeTime(millisecondsTmp,nanosecondsTmp);
	}

	/**
	* Return this + time. A new object is allocated for the result.
	*
	* @param time	the time to add to <code>this</code>
	* @return	the result
	*/
	public final RelativeTime add(RelativeTime time){
	
		long total = getTotalNanos() + time.getTotalNanos();
		setTotalNanos(total);
		
		return new RelativeTime(millisecondsTmp,nanosecondsTmp);
	}

	/**
	*	The purpose of "destination" is unclear, for the result is returned anyway.
	*	
	* Return this + time. If destination is non-null, the result is placed there and dest
	*	is returned. Otherwise a new object is allocated for the result.
	*
	* @param time	the time to add to <code>this</code>
	* @param destination to place the result in
	* @return	the result
	*/
	public RelativeTime add(RelativeTime time, RelativeTime destination){

		long total = getTotalNanos() + time.getTotalNanos();
		setTotalNanos(total);

		if(destination != null){
			destination.set(millisecondsTmp,nanosecondsTmp);
			return destination;
		}			
		
		return new RelativeTime(millisecondsTmp,nanosecondsTmp);
	}


	/**
	* Add this time to an AbsoluteTime. It is almost the same dest.add(this, dest) except
	* that it accounts for(ie. divides by) the frequency. If destination is equal to null,
	* NullPointerException is thrown.
	* @param destination
	*/
	public void addInterarrivalTo(AbsoluteTime destination){

		long total = getTotalNanos() + destination.getTotalNanos();
		setTotalNanos(total);
		destination.set(millisecondsTmp,nanosecondsTmp);
	}

	/**
	* Return the interarrival time that is the result of dividing this interval by its frequency.
	* For a <code>RelativeTime</code>, or a <code>RationalTime</code> with a frequency of 1
	* it just returns <code>this</code>. The interarrival time is necessarily an approximation.
	* @param destination	interarrival time is between <code>this</code> and the destination
	* @return interarrival time
	*/
	public RelativeTime getInterarrivalTime(RelativeTime destination){
		if(destination!=null){
			destination.set(this);
			return destination;
		}
		return this;
	}

	/**
	* @param time relative time to subtract from <code>this</code>
	* @return this-time. A new object is allocated for the result.
	*/
	public final RelativeTime subtract(RelativeTime time){

		long total = getTotalNanos() - time.getTotalNanos();
		setTotalNanos(total);
	
		return new RelativeTime(millisecondsTmp,nanosecondsTmp);
	}

	/**
	* @param time relative time to subtract from <code>this</code>
	* @param destination place to store the result. New object allocated if null
	* @return this-time. A new object is allocated for the result.
	*/
	public RelativeTime subtract(RelativeTime time, RelativeTime destination){

		long total = getTotalNanos() - time.getTotalNanos();
		setTotalNanos(total);
		if(destination != null){
			destination.set(millisecondsTmp,nanosecondsTmp);
			return destination;
		}

		return new RelativeTime(millisecondsTmp,nanosecondsTmp);
	}

	
	/**
	* Return a printable version of this time.
	* Overrides: java.lang.Object.toString() in class java.lang.Object
	* @return  String   a printable version of this time.
	*/
	/*
	public java.lang.String toString(){
		return new String(milliseconds+"ms+"+nanoseconds+"ns");
	}
	*/


}

