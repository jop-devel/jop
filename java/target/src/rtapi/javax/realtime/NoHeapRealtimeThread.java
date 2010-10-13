package javax.realtime;

import javax.safetycritical.annotate.SCJAllowed;
import javax.safetycritical.annotate.SCJProtected;
import javax.safetycritical.annotate.BlockFree;

import static javax.safetycritical.annotate.Level.LEVEL_2;


@SCJAllowed(LEVEL_2)
public class NoHeapRealtimeThread extends RealtimeThread
{
  // These constructors are probably @SCJProtected, but not sure which
  // ones are needed...  and @SCJProtected annotations aren't really
  // relevant to the spec, I think.

  /**
   * TBD: do we use this constructor, which expects a MemoryArea argument?
   */
  public NoHeapRealtimeThread(SchedulingParameters schedule, MemoryArea area)
  {
    // super(schedule, null, null, area, null, null);
  }

  /**
   * TBD: do we use this constructor, which expects a
   * ReleaseParameters argument? 
   */
  public NoHeapRealtimeThread(SchedulingParameters schedule,
                              ReleaseParameters release)
  {
    // super(schedule, release, null, null, null, null);
  }

  /**
   * Creation of thread may block, but starting shall not
   */
  @BlockFree
  public void start()
  {
  }
}
