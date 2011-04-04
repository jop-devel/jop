package javax.realtime;

import javax.safetycritical.annotate.SCJAllowed;
import javax.safetycritical.annotate.SCJProtected;


@SCJAllowed
public class BoundAsyncEventHandler extends AsyncEventHandler
{
  /** Note: since this is only used by infrastructure, we don't
   * specify the MemoryAreaEncloses relationships.
   *

  public BoundAsyncEventHandler(SchedulingParameters scheduling,
                                ReleaseParameters release,
                                MemoryParameters memory,
                                MemoryArea area,
                                ProcessingGroupParameters group,
                                boolean noheap,
                                Runnable logic)
  {
    super(scheduling, release, memory, area, group, noheap, logic);
  }
   */

}
