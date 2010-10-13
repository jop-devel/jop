package javax.safetycritical;

import static javax.safetycritical.annotate.Level.LEVEL_0;

import javax.safetycritical.annotate.BlockFree;
import javax.safetycritical.annotate.SCJAllowed;

/**
 * TBD: Don't we want a method to query the ceiling associated with a
 * particular MonitorControl object?
 * <p>
 * TBD: This was declared to extend
 * javax.realtime.PriorityCeilingEmulation, but that class doesn't
 * exist in our subset, so I've commented out the extends clause (kdn)
 */
//@SCJAllowed(LEVEL_0) AJW THINKS THIS IS A DEAD CLASS
public class PriorityCeilingEmulation {
//   extends javax.realtime.PriorityCeilingEmulation {

  @BlockFree
  //@SCJAllowed(LEVEL_0)
  public int getCeiling(){return 0;}

  @BlockFree
  //@SCJAllowed(LEVEL_0)
  public static PriorityCeilingEmulation instance(int ceiling) {
    return null; // dummy return
  }

  /**
   * TBD: Is this the right way to set thread priorities?  Andy is
   * reworking the synchronization section.  The combined capabilities
   * of setThreadDefaultCeiling() and clearThreadDefaultCeiling()
   * represent one of possibly many different possible solutions.
   * <p>
   * Set the priority ceiling for subsequent allocation of objects by
   * this thread. Use this method instead
   * of javax.realtime.setMonitorControl() because this method
   * localizes its impact to the current thread.
   * <p>
   * It is a programming error to allocate an object that has
   * synchronized methods without enclosing the object's allocation
   * between an invocation of setThreadDefaultCeiling() and
   * clearThreadDefaultCeiling().  Undefined behavior will result.
   * <p>
   * Note that our consensus position regarding JSR 302 is that we do
   * not allow ceiling priorities to change dynamically. 
   * The only valid time to setCeiling() for a newly allocated object
   * is before that object has participated in any synchronization.
   * <p>
   * In an RTSJ-based implementation of JSR 302, the implementation of
   * this method might set a global lock, then set a the global monitor
   * control policy.  The global lock would be released by the
   * clearThreadDefaultCeiling() method.
   */
  @BlockFree
  @SCJAllowed(LEVEL_0)
  public static void setThreadDefaultCeiling(PriorityCeilingEmulation policy)
    throws IllegalStateException
  {
  }

  /**
   * Signal the end of allocations for which a previous invocation of
   * setThreadDefaultCeiling() has relevance.
   * <p>
   * In an RTSJ-based implementation of JSR 302, this would release
   * the global lock that controls changes to the global monitor control
   * policy.  For JSR 302 implementations that are not based directly
   * on RTSJ, this may be a no-op.
   */
  @BlockFree
  @SCJAllowed(LEVEL_0)
  public static void clearThreadDefaultCeiling()
    throws IllegalStateException
  {
  }

  

}
