package javax.realtime;

import static javax.safetycritical.annotate.Level.LEVEL_1;

import javax.safetycritical.annotate.SCJAllowed;
import javax.safetycritical.annotate.SCJProtected;
import javax.safetycritical.annotate.SCJRestricted;

@SCJAllowed
public abstract class ReleaseParameters { // implements java.lang.Cloneable {

  @SCJProtected
  protected ReleaseParameters(RelativeTime cost, RelativeTime deadline,
                              AsyncEventHandler overrunHandler,
                              AsyncEventHandler missHandler)
  { }

  @SCJAllowed
  protected ReleaseParameters()
  { }

  @SCJAllowed(LEVEL_1)
  protected ReleaseParameters(RelativeTime deadline,
                              AsyncEventHandler missHandler)
  { }

  @SCJAllowed(LEVEL_1)
  public Object clone()
  {
    return null;
  }

  @SCJAllowed(LEVEL_1)
  public AsyncEventHandler getDeadlineMissHandler()
  {
    return null;
  }

  /**
   * TBD: whether SCJ makes any use of deadlines or tries to detect
   * deadline overruns.
   * <p>
   * No allocation because RelativeTime is immutable.
   */ 
  @SCJRestricted(maySelfSuspend = false)
  @SCJAllowed(LEVEL_1)
  public RelativeTime getDeadline() {
    return null;
  }
}
