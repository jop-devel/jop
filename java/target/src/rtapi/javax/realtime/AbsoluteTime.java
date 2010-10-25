package javax.realtime;

import javax.safetycritical.annotate.Allocate;
import javax.safetycritical.annotate.BlockFree;
import javax.safetycritical.annotate.MemoryAreaEncloses;
import javax.safetycritical.annotate.SCJAllowed;
import javax.safetycritical.annotate.SCJProtected;
import javax.safetycritical.annotate.SCJRestricted;

import static javax.safetycritical.annotate.Allocate.Area.CURRENT;

/**
 * An object that represents a specific point in time given by milliseconds 
 * plus nanoseconds past some point in time fixed by the clock. For the default 
 * real-time clock the fixed point is the implementation dependent Epoch.
 * The correctness of the Epoch as a time base depends on the real-time clock
 * synchronization with an external world time reference.
 * 
 * A time object in normalized form represents negative time if both components
 * are nonzero and negative, or one is nonzero and negative and the other is zero.
 * For add and subtract negative values behave as they do in arithmetic. 
 */
@SCJAllowed
public class AbsoluteTime extends HighResolutionTime {

  /**
   * Construct an AbsoluteTime object with time millisecond and
   * nanosecond components past the real-time clock's Epoch.  
   * @param ms The desired value for the millisecond component of
   * this. The actual value is the result of parameter normalization. 
   * @param ns The desired value for the nanosecond component of
   * this. The actual value is the result of parameter normalization. 
   */
  @BlockFree
  @SCJAllowed
  @SCJRestricted(maySelfSuspend = false)
  public AbsoluteTime(long millis, int nanos) {
  }

  /**
   * Equivalent to new AbsoluteTime(0,0). 
   */
  @SCJAllowed
  @SCJRestricted(maySelfSuspend = false)
  public AbsoluteTime() {
  }

//	/**
//	 * TBD: Do we want to require MemoryAreaEncloses(inner = {"this"}, outer =
//	 * {"time.getClock()"})? PERC Pico says that Clock must be allocated in
//	 * immortal memory to avoid this "difficulty"?
//	 */
  /**
   * Make a new AbsoluteTime object from the given AbsoluteTime object. 
   * @param The AbsoluteTime object which is the source for the copy. 
   */
  @SCJAllowed
  @SCJRestricted(maySelfSuspend = false)
  public AbsoluteTime(AbsoluteTime time) {
  }

  /**
   * Construct an AbsoluteTime object with time millisecond and
   * nanosecond components past the epoch for clock.  
   * @param ms The desired value for the millisecond component of
   * this. The actual value is the result of parameter normalization. 
   * @param ns The desired value for the nanosecond component of
   * this. The actual value is the result of parameter normalization. 
   * @param clock The clock providing the association for the newly
   * constructed object. 
   */
  @SCJAllowed
  @SCJRestricted(maySelfSuspend = false)
  public AbsoluteTime(long millis, int nanos, Clock clock) {
  }

  /**
   * Equivalent to new AbsoluteTime(0,0,clock). 
   * @param clock The clock providing the association for the newly
   * constructed object. 
   */
  @SCJAllowed
  @SCJRestricted(maySelfSuspend = false)
  public AbsoluteTime(Clock clock) {
    this(0, 0, clock);
  }

  /**
   * Create a new object representing the result of adding millis and
   * nanos to the values from this  and normalizing the result. 
   * @param millis The number of milliseconds to be added to this.
   * @param nanos The number of nanoseconds to be added to this. 
   * @return A new AbsoluteTime object whose time is the normalization
   * of this plus millis and nanos.  
   */
  @Allocate( { CURRENT })
  @SCJAllowed
  @SCJRestricted(maySelfSuspend = false)
  public AbsoluteTime add(long millis, int nanos) {
    return add(millis, nanos, null);
  }

  /**
   * Return an object containing the value resulting from adding
   * millis and nanos to the values from this  and normalizing the
   * result. 
   * @param millis The number of milliseconds to be added to this.
   * @param nanos The number of nanoseconds to be added to this. 
   * @param dest If dest is not null, the result is placed there and
   * returned. Otherwise, a new object is allocated for the result.  
   * @return the result of the normalization of this plus millis and
   * nanos in dest if dest is not null, otherwise the result is
   * returned in a newly allocated object.  
   */
  @SCJAllowed
  @SCJRestricted(maySelfSuspend = false)
  public AbsoluteTime add(long millis, int nanos, AbsoluteTime dest) {
    return null; // dummy return
  }

  /**
   * Create a new instance of AbsoluteTime  representing the result of
   * adding time to the value of this  and normalizing the result. 
   * @param time The time to add to this. 
   * @return A new AbsoluteTime object whose time is the normalization
   * of this plus the parameter time. 
   */
  @Allocate( { CURRENT })
  @SCJAllowed
  @SCJRestricted(maySelfSuspend = false)
  public AbsoluteTime add(RelativeTime time) {
    return add(time);
  }

  /**
   * Return an object containing the value resulting from adding time
   * to the value of this  and normalizing the result.  
   * @param time The time to add to this.
   * @param dest If dest is not null, the result is placed there and
   * returned. Otherwise, a new object is allocated for the result.  
   * @return the result of the normalization of this plus the
   * RelativeTime  parameter time in dest if dest is not null,
   * otherwise the result is returned in a newly allocated object. 
   */
  @SCJAllowed
  @SCJRestricted(maySelfSuspend = false)
  public AbsoluteTime add(RelativeTime time, AbsoluteTime dest) {
    return null;
  }

  /**
   * Create a new instance of RelativeTime  representing the result of
   * subtracting time from the value of this  and normalizing the
   * result. 
   * @param time The time to subtract from this. 
   * @return A new RelativeTime object whose time is the normalization
   * of this minus the AbsoluteTime  parameter time. 
   */
  @Allocate( { CURRENT })
  @SCJAllowed
  @SCJRestricted(maySelfSuspend = false)
  public RelativeTime subtract(AbsoluteTime time) {
    return subtract(time);
  }

  /**
   * Return an object containing the value resulting from subtracting
   * time from the value of this  and normalizing the result. 
   * @param time The time to subtract from this.
   * @param dest If dest is not null, the result is placed there and
   * returned. Otherwise, a new object is allocated for the result. 
   * @return the result of the normalization of this minus the
   * AbsoluteTime  parameter time in dest if dest is not null,
   * otherwise the result is returned in a newly allocated object.  
   */
  @SCJAllowed
  @SCJRestricted(maySelfSuspend = false)
  public RelativeTime subtract(AbsoluteTime time, RelativeTime dest) {
    return null;
  }

  /**
   * Create a new instance of AbsoluteTime  representing the result of
   * subtracting time from the value of this  and normalizing the
   * result.  
   * @param time The time to subtract from this. 
   * @return A new AbsoluteTime object whose time is the normalization
   * of this minus the parameter time.  
   */
  @Allocate( { CURRENT })
  @SCJAllowed
  @SCJRestricted(maySelfSuspend = false)
  public AbsoluteTime subtract(RelativeTime time) {
    return subtract(time);
  }

  /**
   * Return an object containing the value resulting from subtracting
   * time from the value of this  and normalizing the result. 
   * @param time The time to subtract from this.
   * @param dest If dest is not null, the result is placed there and
   * returned. Otherwise, a new object is allocated for the result. 
   * @return the result of the normalization of this minus the
   * RelativeTime  parameter time in dest if dest is not null,
   * otherwise the result is returned in a newly allocated object.  
   */
  @SCJAllowed
  @SCJRestricted(maySelfSuspend = false)
  public AbsoluteTime subtract(RelativeTime time, AbsoluteTime dest) {
    return null;
  }

  /************** unused RTSJ methods ******************************/

  /*
   * Note: it is not "safe" to automatically convert from one clock basis to
   * another.
   */
  public RelativeTime relative(Clock clock) {
    return relative(clock);
  }

  public AbsoluteTime(AbsoluteTime time, Clock clock) {
  }

  public AbsoluteTime absolute(Clock clock, AbsoluteTime dest) {
    return null;
  }

  public AbsoluteTime absolute(Clock clock) {
    return this;
  }

  public RelativeTime relative(Clock clock, RelativeTime dest) {
    return null;
  }
}
