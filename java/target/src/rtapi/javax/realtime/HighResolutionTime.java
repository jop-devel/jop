/*
 * -----------------------------------------------------------------
 *  HighResolutionTime.java :
 *
 * Copyright (C) 2001 TimeSys Corporation, All Rights Reserved.
 *
 * This software is subject to the terms and conditions of the
 * accompanying Common Public License in file CPLicense.  Your use
 * of this software indicates your acceptance of these terms.
 * -----------------------------------------------------------------
 */


package javax.realtime;

/**
 * Class <code>HighResolutionTime</code> is the base class for AbsoluteTime, RelativeTime, RationalTime. 
 *
 * @author  Kalpesh Solanki
 * @version 1.0
*/

public abstract class HighResolutionTime // implements java.lang.Comparable
{
    /*Millisecond part of the time*/  
    // long milliseconds; 
    int milliseconds; 

    /*Nanosecond part of the time*/
    int nanoseconds;

    /*Temporary results*/
    long millisecondsTmp;
    int nanosecondsTmp;
    
    /* for joprt */
	public int getUs() {
		return ((int) milliseconds)*1000 + nanoseconds/1000;
	}

   /**
    * 	Convert this time to an absolute time, relative to some clock. 
    *	Convenient for situations where you really need an absolute time.
    *	Allocates a destination object if necessary. See the derived class
    *	comments for more specific information.
    *
    *   @param	clock	This clock is used to convert this time into absolute time.
    *   @param	dest 	If null, a new object is created and returned as result, else dest is returned.
    *
    */
	/*
    public abstract AbsoluteTime absolute(Clock clock, AbsoluteTime dest);
    
    public abstract AbsoluteTime absolute(Clock clock);
	*/

   /**
    *	Compares this HighResolutionTime with the specified HighResolutionTime.
    *	
    *	@param	time	compares with this time.
    */
   /*
     public int compareTo(HighResolutionTime time)
     {
	long milly = time.getMilliseconds();
	int nany = time.getNanoseconds();
	long mdiff = milliseconds-milly;


	if(mdiff<0)
		return -1;
	else if(mdiff>0)
		return 1;

	int ndiff = nanoseconds - nany;

	if(ndiff<0)
		return -1;
	else if(ndiff>0)
		return 1;

	return 0;
    }
    */

   /**
    *	For the Comparable interface.
    *
    */
   /*
    public int compareTo(java.lang.Object object)
    {
	if(object instanceof HighResolutionTime)
		return this.compareTo((HighResolutionTime)object);
	return -1;	
    }
    */

   /**
    *	Returns true if the argument object has the same values as this.
    *	
    *	@param	time 	Values are compared to this.
    */
   /*
    public boolean equals(HighResolutionTime time)
    {
	long milly = time.getMilliseconds();
	int nany = time.getNanoseconds();
	long mdiff = milliseconds - milly;
	int ndiff = nanoseconds - nany;

	if (ndiff!=0)
		return false;
	if (mdiff!=0)
		return false;
    
    Class thisClass = this.getClass();
    Class timeClass = time.getClass();
    
    if(!(thisClass.equals(timeClass)))
        return false;
    
	return true;
    }
    */

   /**
    *	Returns true if the argument is a HighResolutionTime reference and has the 
    *	same values as this.
    *
    *	@param	object	Values are compared to this.
    */
   /*
    public boolean equals(java.lang.Object object)
    {
	if(object instanceof HighResolutionTime)
		return this.equals((HighResolutionTime)object);
	return false;
    }
    */

   /**
    *	Returns the milliseconds component of this.
    *
    *	@return The milliseconds component of the time past the epoch 
    *		represented by this.
    */
    public final long getMilliseconds()
    {
	return milliseconds;
    }

   /**
    *	Returns nanoseconds component of this.
    *	
    */
    public final int getNanoseconds()
    {
	return nanoseconds;
    }

	/*
    public int hashCode()
    {
	return super.hashCode();
    }
    */

   /**
    *	Changes the time represented by the argument to some time between the 
    *	invocation of the method and the return of the method.
    *
    *	@param	time	The HighResolutionTime which will be set to represent the 
    *			current time.
    */
    public void set(HighResolutionTime time)
    {
	if(time!=null){
		milliseconds = (int) time.getMilliseconds();
		nanoseconds = time.getNanoseconds();
	}
    }

   /**
    *	Sets the millisecond component of this to the given argument.
    *
    *	@param	millis	This value will be the value of the millisecond component
    *			of this at the completion of the call. If millis is negative the 
    *			millisecond value of this is set to negative value. Although logically
    *			this may represent time before the epoch, invalid results may occur if	
    *			a HighResolutionTime represnting time before the epoch is given as a 
    *			parameter to the methods.
    *		
    */
    public void set(long millis)
    {
	milliseconds = (int) millis;
	nanoseconds = 0;
    }

   /**
    *	Sets the millisecond and nanosecond components of this.
    *
    *	@param	millis	value to set millisecond part of this.
    *	@param	nanos 	value to set nanosecond part of this.
    */
    public void set(long millis, int nanos)
    {
	milliseconds = (int) millis;
	nanoseconds = nanos;
    }

    /**
     * Behaves exactly like <code>target.wait()</code> but with the enhancement 
     * thatit waits with a precision of <code>HighResolutionTime</code>
     *
     * @param target The object on which to wait. The current thread must have a
     * lock on the object.
     * @param time The time for which to wait. If this is <code>RelativeTime(0,0)</code>
     * then wait indefinitely.
     *
     * @throws InterruptedException If another threads interrupts this thread 
     * while its waiting.
     *
     * @see Object#wait
     * @see Object#wait(long)
     * @see Object#wait(long,int)
     */
    /*
    public static void waitForObject(Object target, HighResolutionTime time) 
                    throws InterruptedException
    {
        RelativeTime waitPeriod = time.relative(Clock.getRealtimeClock());
        waitForObject0(target, waitPeriod.getMilliseconds(), waitPeriod.getNanoseconds());
    }
    */

	/*
    private static native void waitForObject0(Object target, long millis, long nanos);
	*/
	
   /**
    *	Returns total nanoseconds of this.
    *
    */
    long getTotalNanos()
    {
	return (milliseconds*1000000 + nanoseconds);
    }    

   /**
    *	Sets millisecond and nanoseconds part of this using total.
    *	
    *	@param	total	Total nanoseconds.
    */
    void setTotalNanos(long total)
    {

	millisecondsTmp = total/1000000;
	nanosecondsTmp = (int)(total - millisecondsTmp*1000000);
    }

	/*
    public abstract RelativeTime relative(Clock clock);
    public abstract RelativeTime relative(Clock clock, RelativeTime destination);
    */

}


