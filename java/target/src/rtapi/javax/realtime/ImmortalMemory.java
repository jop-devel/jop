package javax.realtime;

import javax.safetycritical.annotate.SCJAllowed;
import javax.safetycritical.annotate.SCJRestricted;

@SCJAllowed
public final class ImmortalMemory extends MemoryArea
{
  @SCJAllowed
  @SCJRestricted(maySelfSuspend = false)
  public static ImmortalMemory instance()
  {
    return null; // dummy return
  }

  @SCJAllowed
  @SCJRestricted(maySelfSuspend = false)
  public void enter(Runnable logic)
  {
  }

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
}
