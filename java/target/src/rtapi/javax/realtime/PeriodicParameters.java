package javax.realtime;

import javax.safetycritical.annotate.BlockFree;
import javax.safetycritical.annotate.SCJAllowed;
import static javax.safetycritical.annotate.Level.LEVEL_1;
import javax.safetycritical.annotate.MemoryAreaEncloses;

@SCJAllowed
public class PeriodicParameters extends ReleaseParameters {

  /**
   * Construct a new object within the current memory area.
   * @parameter start isrelative to the start of the mission. A null value defaults to an offset of zero milliseconds.
   *
   * @parameter period is the time between each release of the associated schedulable object.
   *            The default deadline is the same value as the period. The default handler is null.
   *
   * @throws IllegalArgumentException if period is null.
   */
  @SCJAllowed
  @BlockFree
  public PeriodicParameters(HighResolutionTime start,
                            RelativeTime period)
  { super(null,null,null,null); }


  /**
   * Construct a new object within the current memory area.
   * @parameter start isrelative to the start of the mission. A null value defaults to an offset of zero milliseconds.
   *
   * @parameter period is the time between each release of the associated schedulable object.
   *
   * @parameter deadline is an offset from the release time by which the release should finish. 
   *            A null deadline indicates the same value as the period.
   * @parameter handler is the async event handler to be release if the associated schedulable object
   *            misses its deadline. A null parameter indicates that no handler should be release.
   *
   * @throws IllegalArgumentException if period is null.
   */
  @SCJAllowed
  @BlockFree
  public PeriodicParameters(HighResolutionTime start,
                            RelativeTime period, 
                            RelativeTime deadline,
                            AsyncEventHandler handler)
  { super(null,null,null,null); }
  /**
   * @return Returns the object originally passed in to the constructor, which is
   *         known to reside in a memory area that encloses this.
   */
  @BlockFree
  @SCJAllowed
  public HighResolutionTime getStart() {
    return null;
  }
  
 /**
   * @return  Returns the object originally passed in to the constructor, which is
   *          known to reside in a memory area that encloses this.
   */
  @BlockFree
  @SCJAllowed
  public RelativeTime getPeriod() {
    return null;
  }
  
   /**
   * @return  Returns the object originally passed in to the constructor, which is
   *          known to reside in a memory area that encloses this.
   */
  @BlockFree
  @SCJAllowed
  public RelativeTime getDeadline() {
    return null;
  }
  
     /**
   * @return  Returns the object originally passed in to the constructor, which is
   *          known to reside in a memory area that encloses this.
   */
  @BlockFree
  @SCJAllowed
  public AsyncEventHandler getHandler() {
    return null;
  }
}
