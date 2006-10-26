/*
 * -----------------------------------------------------------------
 * AbsoluteTime.java :
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
  An object that represents a specific point in time given by
  milliseconds plus nanoseconds past the epoch
  (January 1, 1970, 00:00:00 GMT).
  This representation was designed to be compatible with
  the standard Java representation of an absolute time in the
  {@link java.util.Date} class.

  <p><b>Caution:</b> This class is explicitly unsafe in multithreaded
  situations when it is being changed.  No synchronization is done.  It
  is assumed that users of this class who are mutating instances will be
  doing their own synchronization at a higher level.
 */
public class AbsoluteTime extends HighResolutionTime
{
	/**
	* Equal to new AbsoluteTime(0,0).
	*/
 	public AbsoluteTime(){
		
		this(0,0);
	}

    /**
     * make a new AbsolutTime object from the given AbsoluteTime object
     * @param  time  The AbsolutTime object as the source for the copy
     */
    public AbsoluteTime(AbsoluteTime time){
	if(time == null){
	    // throw new IllegalArgumentException("Illegal null argument");
	}
	this.milliseconds = (int) time.getMilliseconds();
	this.nanoseconds = time.getNanoseconds();
    }

	/**
	* Equivalent to new AbsoluteTime (date.getTime(),0)
	* @param  date   The java.util.Data representation of the time past the epoch
	*/	
	/*
 	public AbsoluteTime(java.util.Date date){
	    if(date == null){
		throw new IllegalArgumentException("Illegal null argument");
	    }
	    this.milliseconds = date.getTime();
	    this.nanoseconds = 0;
	}
	*/

	/**
	* Construct an AbsoluteTime object which means a time millis milliseconds plus
	* nanos nanoseconds past00:00:00 GMT on January 1, 1970.
	* @param millis The milliseconds component of the time past the epoch
	* @param nanos  The nanosecond component of the time past the epoch
	*/
 	public AbsoluteTime(long millis, int nanos)
	{
	    this.milliseconds = (int) millis;
	    this.nanoseconds = nanos;
	    
	}

    /**
     * Convert this time to an absolute time. For an AbsoluteTime, this is really easy:
     * it just return itself. Presumes that this time is already relative to the given clock.
     * @param clock       Clock on which this is based
     * @param destination Converted to an absolute time
     * @return this
     */
    /*
    public AbsoluteTime absolute(Clock clock, AbsoluteTime destination)
    {
	
	if(destination != null){
	    destination.set(milliseconds,nanoseconds);
	}
	
	return this;			
    }
    */

    /**
     * Convert this time to an absolute time relative to a given clock.
     * @param clock       Clock on which this is based
     * @return this
     */
    /*
    public AbsoluteTime absolute(Clock clock){
	return this;
    }
    */

	/*
    public RelativeTime relative(Clock clock)
    {
	if(clock==null){
	    clock = Clock.getRealtimeClock();
	}

	
	return subtract(clock.getTime());
    }
    */

	/*
    public RelativeTime relative(Clock clock, RelativeTime destination)
    {
	if(clock==null){
	    clock = Clock.getRealtimeClock();
	}
	
	return subtract(clock.getTime(),destination);	
    }
	*/


	/**
	* add millis and nanos to this. A new object is allocated for the result
	* @param millis  the milliseconds value to be added to this
	* @param nanos   the nanoseconds value to be added to this
	* @return  the result after adding this with millis and nanos.
	*/
 	public AbsoluteTime add(long millis, int nanos){
		
		long total = getTotalNanos()+millis*1000000+nanos;
		setTotalNanos(total);

		return new AbsoluteTime(millisecondsTmp,nanosecondsTmp);
	}

	/**
	* If a destination is non-null, the result is placed there and the destination is returned.
	* Otherwise a new object is allocated for the result.
	* @param millis	milliseconds
	* @param nanos	nanoseconds
	* @return	the result
	*/	
	public AbsoluteTime add(long millis, int nanos, AbsoluteTime destination)
	{
		long total = getTotalNanos()+millis*1000000+nanos;
		setTotalNanos(total);

		if(destination != null){
			destination.set(millisecondsTmp,nanosecondsTmp);
			return destination;
		}
		
		return new AbsoluteTime(millisecondsTmp,nanosecondsTmp);		
	}

	/**
	* Return this + time. A new object is allocated for the result.
	*
	* @param time	the time to add to <code>this</code>
	* @return	the result
	*/
 	public final AbsoluteTime add(RelativeTime time){

		long total = getTotalNanos()+time.getTotalNanos();
		setTotalNanos(total);

		return new AbsoluteTime(millisecondsTmp,nanosecondsTmp);
	}	

	/**
	* The purpose of "destination" is unclear, for the result is returned anyway.
	*
	* Return this + time. If destination is non-null, the result is placed there and
	* destination is returned. Otherwise a new object is allocated for the result.
	*
	* @param time	the time to add to <code>this</code>
	* @param destination to place the result in
	* @return  the result
	*/
 	public AbsoluteTime add(RelativeTime time, AbsoluteTime destination){
		long total = getTotalNanos()+time.getTotalNanos();
		setTotalNanos(total);
		
		if(destination != null){
			destination.set(millisecondsTmp,nanosecondsTmp);
			return destination;
		}

		return new AbsoluteTime(millisecondsTmp,nanosecondsTmp);
			
	}

	/**
	* @return  The time past the epoch represented by this as a java.util.Date.
	*/
	/*
	public java.util.Date getDate(){
		return new Date(milliseconds);
	}
	*/	

       /**
	*   Change the time represented by this.
	*   @param date	 java.util.Date which becomes the time represented by this
	*		 after the completion of this method.
	*/
    /*
	public void set(java.util.Date date){
		set(date.getTime(),0);
	}
	*/

	/**
	* @param time absolute time to subtract from <code>this</code>
	* @return this-time. A new object is allocated for the result.
	*/
  	public final RelativeTime subtract(AbsoluteTime time){

		long totaldiff = getTotalNanos()-time.getTotalNanos();
		setTotalNanos(totaldiff);

		return new RelativeTime(millisecondsTmp,nanosecondsTmp);
	}

	/**
	* @param time absolute time to subtract from <code>this</code>
	* @param destination place to store the result. New object allocated if null
	* @return this-time. A new object is allocated for the result.
	*/
	public final RelativeTime subtract(AbsoluteTime time, RelativeTime destination){

		long totaldiff = getTotalNanos()-time.getTotalNanos();
		setTotalNanos(totaldiff);
	
		if(destination != null){
			destination.set(millisecondsTmp,nanosecondsTmp);
			return destination;
		}

		return new RelativeTime(millisecondsTmp,nanosecondsTmp);
		
	}

	/**
	* @param time relative time to subtract from <code>this</code>
	* @return this-time. A new object is allocated for the result.
	*/
	public final AbsoluteTime subtract(RelativeTime time){

		long totaldiff = getTotalNanos()-time.getTotalNanos();
		setTotalNanos(totaldiff);
	
		return new AbsoluteTime(millisecondsTmp,nanosecondsTmp);
	
	}
		
	/**
	* @param time relative time to subtract from <code>this</code>
	* @param destination place to store the result. New object allocated if null
	* @return this-time. A new object is allocated for the result.
	*/
	public AbsoluteTime subtract(RelativeTime time, AbsoluteTime destination){

		long totaldiff = getTotalNanos()-time.getTotalNanos();
		setTotalNanos(totaldiff);

		if(destination != null){
			destination.set(millisecondsTmp,nanosecondsTmp);
			return destination;
		}

		return new AbsoluteTime(millisecondsTmp,nanosecondsTmp);
	
	}

	/** Return a printable version of this time, in a format that matches
	* java.util.Date.toString() with a postfix to the detail the sub-second value
	* @return String object converted from this.
	*/
	/*
	public java.lang.String toString(){

		long tmp = (long)(milliseconds/1000);
		int milli = (int)(milliseconds - tmp*1000);
		 
		return (getDate()).toString()+"+"+milli+"ms "+nanoseconds+"ns";
	}
	*/

}




