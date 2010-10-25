package javax.realtime;

import static javax.safetycritical.annotate.Allocate.Area.CURRENT;

import javax.safetycritical.annotate.Allocate;
import javax.safetycritical.annotate.SCJAllowed;
import javax.safetycritical.annotate.SCJRestricted;

/**
 * An object that represents a time interval milliseconds/10^3 +
 * nanoseconds/10^9 seconds long that is divided into subintervals by
 * some frequency. This is generally used in periodic events, threads,
 * and feasibility analysis to specify periods where there is a basic
 * period that must be adhered to strictly (the interval), but within
 * that interval the periodic events are supposed to happen frequency
 * times, as uniformly spaced as possible, but clock and scheduling
 * jitter is moderately acceptable.  
 *
 */
@SCJAllowed
public class RelativeTime extends HighResolutionTime {

  /**
   * Equivalent to new RelativeTime(0,0).
   */
  @SCJAllowed
  @SCJRestricted(maySelfSuspend = false)
  public RelativeTime() {
  }

  /**
   * Construct a RelativeTime object representing an interval based on
   * the parameter millis plus the parameter nanos. 
   * 
   * @param ms The desired value for the millisecond component of
   * this. The actual value is the result of parameter normalization. 
   * @param ns The desired value for the nanosecond component of
   * this. The actual value is the result of parameter normalization. 
   */
  @SCJAllowed
  @SCJRestricted(maySelfSuspend = false)
  public RelativeTime(long ms, int ns) {
  }

  /**
   * Equivalent to new RelativeTime(0,0,clock). 
   * 
   * @param clock The clock providing the association for the newly
   * constructed object. 
   */
  @SCJAllowed
  @SCJRestricted(maySelfSuspend = false)
  public RelativeTime(Clock clock) {
  }

  /**
   * Construct a RelativeTime object representing an interval based on
   * the parameter millis plus the parameter nanos. 
   * 
   * @param ms The desired value for the millisecond component of
   * this. The actual value is the result of parameter normalization. 
   * @param ns The desired value for the nanosecond component of
   * this. The actual value is the result of parameter normalization. 
   * @param clock The clock providing the association for the newly
   * constructed object. 
   */
  @SCJAllowed
  @SCJRestricted(maySelfSuspend = false)
  public RelativeTime(long ms, int ns, Clock clock) {
  }
  
//	/**
//	 * TBD: Do we want to require MemoryAreaEncloses(inner = {"this"}, outer =
//	 * {"time.getClock()"})? PERC Pico says that Clock must be allocated in
//	 * immortal memory to avoid this "difficulty"?
//	 */

  /**
   * Make a new RelativeTime object from the given RelativeTime object. 
   * 
   * @param time The RelativeTime object which is the source for the copy.
   */
  @SCJAllowed
  @SCJRestricted(maySelfSuspend = false)
  public RelativeTime(RelativeTime time) {
  }


  /**
   * Create a new object representing the result of adding millis and
   * nanos to the values from this  and normalizing the result. 
   * @param millis The number of milliseconds to be added to this.
   * @param nanos The number of nanoseconds to be added to this. 
   * @return A new RelativeTime object whose time is the normalization
   * of this plus millis and nanos.  
   */
  @Allocate( { CURRENT })
  @SCJAllowed
  @SCJRestricted(maySelfSuspend = false)
  public RelativeTime add(long millis, int nanos) {
    return add(millis, nanos);
  }
  
  /**
   * Create a new instance of RelativeTime  representing the result of
   * adding time to the value of this  and normalizing the result. 
   * @param time The time to add to this. 
   * @return A new RelativeTime object whose time is the normalization
   * of this plus millis and nanos.  
   */
  @Allocate( { CURRENT })
  @SCJAllowed
  @SCJRestricted(maySelfSuspend = false)
  public RelativeTime add(RelativeTime time) {
    return add(time);
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
  public RelativeTime add(long millis, int nanos, RelativeTime dest) {
    return null;
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
  public RelativeTime add(RelativeTime time, RelativeTime dest) {
    return null;
  }

  /**
   * Create a new instance of RelativeTime  representing the result of
   * subtracting time from the value of this  and normalizing the
   * result. 
   * @param time The time to subtract from this.
   * @return A new RelativeTime object whose time is the normalization
   * of this minus the parameter time  parameter time. 
   */
  @Allocate( { CURRENT })
  @SCJAllowed
  @SCJRestricted(maySelfSuspend = false)
  public RelativeTime subtract(RelativeTime time) {
    return subtract(time);
  }


  /**
   * Return an object containing the value resulting from subtracting
   * the value of time from the value of this  and normalizing the
   * result. 
   * @param time The time to subtract from this.
   * @param dest If dest is not null, the result is placed there and
   * returned. Otherwise, a new object is allocated for the result.  
   * @return the result of the normalization of this minus the
   * RelativeTime  parameter time in dest if dest is not null,
   * otherwise the result is returned in a newly allocated object.  
   */
  @SCJAllowed
  @SCJRestricted(maySelfSuspend = false)
  public RelativeTime subtract(RelativeTime time, RelativeTime dest) {
    return null;
  }

  /************** unused RTSJ methods ******************************/
  
  /**
   * Note: it is not "safe" to automatically convert from one clock basis to
   * another.
   */
  public AbsoluteTime absolute(Clock clock) {
    return absolute(clock);
  }

  /**
   * Note: it is not "safe" to automatically convert from one clock basis to
   * another.
   */
  public RelativeTime relative(Clock clock) {
    return null; // dummy return
  }
  
  // clock conversion
  public RelativeTime(RelativeTime time, Clock clock) {
  }
  public AbsoluteTime absolute(Clock clock, AbsoluteTime destination) {
    return null;
  }
  public RelativeTime relative(Clock clock, RelativeTime destination) {
    return null;
  }
}
