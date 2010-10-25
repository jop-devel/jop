package javax.realtime;

import static javax.safetycritical.annotate.Level.LEVEL_0;

import javax.safetycritical.annotate.SCJAllowed;
import javax.safetycritical.annotate.SCJProtected;
import javax.safetycritical.annotate.SCJRestricted;

@SCJAllowed
public class LTMemory extends ScopedMemory
{
  /**
   * This is not SCJAllowed because we don't want to instantiate
   * these.  Safety-critical Java developers should instantiate
   * SafetyMemory instead.
   */
  @SCJProtected
  public LTMemory(long size) { super(size); }

  @SCJProtected
  public LTMemory(SizeEstimator estimator) { super(estimator); }

  /**
   * In vanilla RTSJ, enter() is not necessarily block-free because
   * entering an LTMemory region may have to wait for the region to be
   * finalized.  However, a complaint implementation of JSR 302 shall
   * provide a block-free implementation of enter.  Note that JSR 302
   * specifies that finalization of LTMemory regions is not performed.
   */
  @SCJAllowed(LEVEL_0)
  @SCJRestricted(maySelfSuspend = false)
  public void enter(Runnable logic) {}

  @SCJAllowed
  @SCJRestricted(maySelfSuspend = false)
  public long memoryConsumed()
  {
    return 0L; // dummy return
  }

  @SCJAllowed
  @SCJRestricted(maySelfSuspend = false)
  public long memoryRemaining()
  {
    return 0L; // dummy return
  }

  @SCJAllowed
  @SCJRestricted(maySelfSuspend = false)
  public long size()
  {
    return 0L; // dummy return
  }

  /**
   * @see javax.realtime.ScopedAllocationContext#resize(long)
   */
  @Override
  @SCJAllowed
  @SCJRestricted(maySelfSuspend = false)
  public void resize(long size)
    throws IllegalStateException
  {
  }
}
