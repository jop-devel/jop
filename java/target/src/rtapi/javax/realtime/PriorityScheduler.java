package javax.realtime;

import javax.safetycritical.annotate.BlockFree;
import javax.safetycritical.annotate.SCJAllowed;

import static javax.safetycritical.annotate.Level.LEVEL_0;
import static javax.safetycritical.annotate.Level.LEVEL_1;
import static javax.safetycritical.annotate.Level.LEVEL_2;

@SCJAllowed(LEVEL_1)
public class PriorityScheduler extends Scheduler
{
  /**
   * Priority-based dispatching is supported at Levels 1 and 2.
   * The only access to the priority scheduler is for obtaining the
   * maximum priority. 
   *
   * No allocation here, because the primordial instance is presumed
   * allocated at within the <clinit> code.
   */
  public static PriorityScheduler instance()
  {
    return null;
  }

  @BlockFree
  @SCJAllowed(LEVEL_1)
  public int getMaxPriority()
  {
    return 39;
  }

  @BlockFree
  @SCJAllowed(LEVEL_1)
  public int getNormPriority()
  {
    return 25;
  }

  @BlockFree
  @SCJAllowed(LEVEL_1)
  public int getMinPriority()
  {
    return 11;
  }
}
