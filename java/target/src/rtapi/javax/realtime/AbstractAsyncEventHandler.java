package javax.realtime;

import javax.safetycritical.annotate.Allocate;
import javax.safetycritical.annotate.BlockFree;
import javax.safetycritical.annotate.SCJAllowed;
import javax.safetycritical.annotate.SCJProtected;

import static javax.safetycritical.annotate.Level.LEVEL_0;


@SCJAllowed(LEVEL_0)
public abstract class AbstractAsyncEventHandler implements Schedulable
{

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
  


  public final void setDaemon(boolean on) {}
}
