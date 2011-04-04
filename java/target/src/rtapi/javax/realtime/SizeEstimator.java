package javax.realtime;


import javax.safetycritical.annotate.SCJAllowed;
import javax.safetycritical.annotate.SCJRestricted;

/**
 */
@SCJAllowed
public final class SizeEstimator
{
  /**
   * 
   */
  @SCJAllowed
  @SCJRestricted(maySelfSuspend = false)
  public SizeEstimator() {}

  /**
   * JSR 302 tightens the semantic requirements on the implementation
   * of getEstimate.  For compliance with JSR 302, getEstimate() must
   * return a conservative upper bound on the amount of memory
   * required to represent all of the memory reservations associated
   * with this SizeEstimator object.
   * @return 
   */
  @SCJAllowed
  @SCJRestricted(maySelfSuspend = false)
  public long getEstimate() { return 0; }

  /**
   * @param clazz
   * @param num
   */
  @SCJAllowed
  @SCJRestricted(maySelfSuspend = false)
  public void reserve(Class clazz, int num) {}

  /**
   * @param size
   */
  @SCJAllowed
  @SCJRestricted(maySelfSuspend = false)
  public void reserve(SizeEstimator size) {}

  /**
   * @param size
   * @param num
   */
  @SCJAllowed
  @SCJRestricted(maySelfSuspend = false)
  public void reserve(SizeEstimator size, int num) {}

  /**
   * @param length
   */
  @SCJAllowed
  @SCJRestricted(maySelfSuspend = false)
  public void reserveArray(int length) {}

  /**
   * @param length
   * @param type
   */
  @SCJAllowed
  @SCJRestricted(maySelfSuspend = false)
  public void reserveArray(int length, Class type) {}
}
