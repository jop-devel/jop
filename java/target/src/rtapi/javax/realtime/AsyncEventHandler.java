package javax.realtime;

import javax.safetycritical.annotate.SCJAllowed;
import javax.safetycritical.annotate.SCJProtected;

import static javax.safetycritical.annotate.Level.LEVEL_0;


/**
 * AsyncEventHandler
 *
 */
@SCJAllowed(LEVEL_0)
public class AsyncEventHandler extends AbstractAsyncEventHandler
{
  /*
   * Not @SCJAllowed
   *
  public AsyncEventHandler(SchedulingParameters scheduling,
               ReleaseParameters release,
               MemoryParameters memory,
               MemoryArea area,
               ProcessingGroupParameters group,
                           boolean noheap,
               Runnable logic)
  {
  }
  */

  // not scj allowed
  @Override
  public MemoryParameters getMemoryParameters() { return null; }

  /**
   * @return A reference to the associated ReleaseParameter object.
   */
  //@BlockFree
  //@SCJAllowed
  @Override
  public ReleaseParameters getReleaseParameters() { return null; }

  /**
   *  @return A reference to the associated SchedulingParameter object.
   */
  //@SCJAllowed
  @Override
  public SchedulingParameters getSchedulingParameters() { return null; }
  
 /**
   * Infrastructure code.
   * Must not be called.
   */
  @Override
  @SCJProtected
  public final void run() {}

  /**
   * 
   */
  @SCJAllowed
  public void handleAsyncEvent() {}
}
