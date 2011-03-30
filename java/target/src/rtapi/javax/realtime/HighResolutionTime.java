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

  /**
   * the clock associated with this
   */
  Clock clock;

  /**
   * milliseconds part of the time.
   */
  long milli;

  /**
   * nanoseconds part of the time.
   */
  int nano;

  /**
   * 
   * @return A reference to the clock associated with this.
   */
  @SCJAllowed
  @SCJRestricted(maySelfSuspend = false)
  public Clock getClock() {
    return null; // dummy return
  }

  /**
   * 
   * @return The milliseconds component of the time represented by this.
   */
  @SCJAllowed
  @SCJRestricted(maySelfSuspend = false)
  public final long getMilliseconds() {
    return -1L; // dummy return
  }

  /**
   * 
   * @return The nanoseconds component of the time represented by this.
   */
  @SCJAllowed
  @SCJRestricted(maySelfSuspend = false)
  public final int getNanoseconds() {
    return -1; // dummy return
  }
  
  /**
   * Change the value represented by this to that of the given time.
   * @param time The new value for this. 
   */
  @SCJAllowed
  @SCJRestricted(maySelfSuspend = false)
  public void set(HighResolutionTime time) {
  }

  /**
   * Sets the millisecond component of this to the given argument, and
   * the nanosecond component of this to 0. 
   * @param millis This value shall be the value of the millisecond
   * component of this at the completion of the call. 
   */
  @SCJAllowed
  @SCJRestricted(maySelfSuspend = false)
  public void set(long millis) {
    set(millis, 0);
  }

  /**
   * Sets the millisecond and nanosecond components of this.
   * @param millis The desired value for the millisecond component of
   * this at the completion of the call. The actual value is the
   * result of parameter normalization. 
   * @param nanos The desired value for the nanosecond component of
   * this at the completion of the call. The actual value is the
   * result of parameter normalization.  
   */
  @SCJAllowed
  @SCJRestricted(maySelfSuspend = false)
  public void set(long millis, int nanos) {
  }
	
  /**
   * Returns true if the argument object  has the same type and values as this.
   * @param time Value compared to this.
   * @return true  if the parameter object is of the same type and has
   * the same values as this. 
   */
  @SCJAllowed
  @SCJRestricted(maySelfSuspend = false)
  public boolean equals(HighResolutionTime time) {
    return false; // dummy return
  }

  /**
   * Returns true if the argument object  has the same type and values as this.
   * @param object Value compared to this.
   * @return true  if the parameter object is of the same type and has
   * the same values as this. 
   */
  @SCJAllowed
  @SCJRestricted(maySelfSuspend = false)
  public boolean equals(java.lang.Object object) {
    return false; // dummy return
  }
  
  /**
   * Compares this HighResolutionTime  with the specified
   * HighResolutionTime time. 
   * @param time Compares with the time of this.
   * @return
   */
  @SCJAllowed
  @SCJRestricted(maySelfSuspend = false)
  public int compareTo(HighResolutionTime time) {
    return -1; // dummy return
  }
  
  
  /**
   * Compares this HighResolutionTime  with the specified object.
   * @param object Compares with the time of this.
   * @return
   */
  @SCJAllowed
  @SCJRestricted(maySelfSuspend = false)
  public int compareTo(java.lang.Object object) {
    return -1; // dummy return
  }
  
  /**
   * Returns a hash code for this object in accordance with the
   * general contract of Object.hashCode(). 
   * @return The hashcode value for this instance. 
   */
  @SCJAllowed
  @SCJRestricted(maySelfSuspend = false)
  public int hashCode() {
    return -1; // dummy return
  }

  /**
   * Behaves exactly like target.wait() but with the enhancement that
   * it waits with a precision of HighResolutionTime. 
   * @param target The object on which to wait. The current thread
   * must have a lock on the object. 
   * @param time The time for which to wait. If it is
   * RelativeTime(0,0) then wait indefinitely. If it is null then wait
   * indefinitely. 
   * @throws java.lang.InterruptedException
   */
  @SCJAllowed(LEVEL_2)
  public static void waitForObject(java.lang.Object target,
                                   HighResolutionTime time)
    throws java.lang.InterruptedException {
  }

  /************** unused RTSJ methods ******************************/
  
  /**
   * Note: it is not "safe" to automatically convert from one clock basis to
   * another.
   */
  public abstract RelativeTime relative(Clock clock);
  public abstract RelativeTime relative(Clock clock, RelativeTime time);
  public abstract AbsoluteTime absolute(Clock clock);
  public abstract AbsoluteTime absolute(Clock clock, AbsoluteTime dest);
  
  // We do not allow to set the clock
  void setClock(Clock clock) {
    this.clock = clock;
  }

}
