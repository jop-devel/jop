package javax.realtime;

import static javax.safetycritical.annotate.Level.LEVEL_1;
import static javax.safetycritical.annotate.Level.LEVEL_2;

import javax.safetycritical.annotate.SCJAllowed;
import javax.safetycritical.annotate.SCJProtected;
import javax.safetycritical.annotate.SCJRestricted;


@SCJAllowed(LEVEL_1)
public class RealtimeThread extends Thread implements Schedulable {
  // public RealtimeThread(SchedulingParameters scheduling,
  // ReleaseParameters release,
  // MemoryParameters memory,
  // MemoryArea area,
  // ProcessingGroupParameters group,
  // java.lang.Runnable logic)
  // { // skeleton
  // }

  @SCJProtected
  public RealtimeThread(SchedulingParameters schedule, MemoryArea area) {
  }
  
  @SCJProtected
  public RealtimeThread() {
  }
  
  public void deschedulePeriodic() {
  }
  
  /**
   * Allocates no memory. Returns an object that resides in the current
   * mission's MissionMemory.
   */
  @SCJAllowed(LEVEL_2)
  @SCJRestricted(maySelfSuspend = false)
  public static RealtimeThread currentRealtimeThread() {
    return null; // dummy return
  }

  /**
   * Allocates no memory. The returned object may reside in scoped memory,
   * within a scope that encloses the current execution context.
   */
  @SCJAllowed(LEVEL_1)
  @SCJRestricted(maySelfSuspend = false)
  public static MemoryArea getCurrentMemoryArea() {
    return null; // dummy return
  }

  @SCJAllowed(LEVEL_2)
  @SCJRestricted(maySelfSuspend = true)
  public static void sleep(javax.realtime.HighResolutionTime time)
    throws InterruptedException {};

  /**
   * Allocates no memory. Does not allow this to escape local variables. The
   * returned object may reside in scoped memory, within a scope that encloses
   * this.
   */
  @SCJAllowed(LEVEL_2)
  @SCJRestricted(maySelfSuspend = false)
  public MemoryArea getMemoryArea() {
    return null; // dummy return
  }

  /**
   * Not @SCJAllowed because ThreadConfigurationParameters releases
   * MemoryParameters. 
   */
   public MemoryParameters getMemoryParameters() {
     return null; // dummy return
   }

  /**
   * Allocates no memory. Does not allow this to escape local variables. The
   * returned object may reside in scoped memory, within a scope that encloses
   * this.
   * <p>
   * No allocation because ReleaseParameters are immutable.
   */
  //@BlockFree
  //@SCJAllowed(LEVEL_2)
  public ReleaseParameters getReleaseParameters() {
    return null; // dummy return
  }

  /**
   * Allocates no memory. Does not allow this to escape local variables. The
   * returned object may reside in scoped memory, within a scope that encloses
   * this.
   * <p>
   * No allocation because SchedulingParameters are immutable.
   */
  //@BlockFree
  //@SCJAllowed(LEVEL_2)
  public SchedulingParameters getSchedulingParameters()
  {
    return null; // dummy return
  }

  /**
   * Allocates no memory. Treats the implicit this argument as a variable
   * residing in scoped memory. 
   */
  public void start() {}
  
  public void release() {}

  public static boolean waitForNextRelease()
  {
    return false; // dummy return
  }

  public static int getMemoryAreaStackDepth()
  {
    return -1;
  }
  
  public static MemoryArea getOuterMemoryArea(int delta)
  {
    return null;
  }
  
}
