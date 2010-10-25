
package javax.safetycritical;

import static javax.safetycritical.annotate.Level.LEVEL_1;

import javax.realtime.SizeEstimator;
import javax.safetycritical.annotate.Allocate;
import javax.safetycritical.annotate.BlockFree;
import javax.safetycritical.annotate.MemoryAreaEncloses;
import javax.safetycritical.annotate.SCJAllowed;
import javax.safetycritical.annotate.Allocate.Area;

/**
 * MissionMemory
 *
 */
@SCJAllowed
class MissionMemory extends ManagedMemory
{
  /**
   * @param size is the amount of memory that this area can hold.
   */
  MissionMemory(long size) { super(size); }

  /**
   * @param size is the amount of memory that this area can hold.
   */
  MissionMemory(SizeEstimator estimator) { super(estimator); }

  /**
   * @return the manager for this memory area.
   */
  @Override
  @BlockFree
  MissionManager getManager() { return null; }
  
  /**
   * Override the portal getter so that the portal object can be
   * shared.
   * <p>
   * TBD: is this comment correct?  looks like a cut-and-paste error
   * from setPortal().
   */
  @Override
  @SCJAllowed(LEVEL_1)
  public synchronized Object getPortal() { return null; }
  
  /**
   * Override the portal getter so that the portal object can be shared.
   */
  @Override
  @SCJAllowed(LEVEL_1)
  public synchronized void setPortal(Object value) {}
  
  /**
   * run is the main routine that performs initialization, starting of the
   * system, cleanup and eventually restarting of a mission.
   * <p>
   * This method is final, subclasses cannot overwrite it. Instead, subclasses
   * can overwrite method {@link initialize}, which is called from run().
   * <p>
   * TBD: I don't really mean to say that this encloses logic.  I
   * think I really want to say that this.scope-level encloses logic.
   */
  @MemoryAreaEncloses(inner = {"logic"}, outer = {"this"})
  @SCJAllowed()
  public final void enter(Runnable logic) {}

  /**
   * Truncate the size of the backing store associated with this
   * MissionMemory object to new_size.
   *
   * @throws IllegalStateException if the objects already allocated
   * within this MissionMemory consume more than new_size bytes.
   *
   * @throws IllegalArgumentException if new_size is larger than the
   * current size of the MissionMemory.
   */
  public final void resize(long new_size) {}

  /**
   * A string representation of the MissionMemory
   * <p>
   * TBD: do we want to allocate the string in the current scope?  Or
   * we could permanently allocate the string representation in the
   * constructor, and simply return a reference to a single shared
   * String object.
   *
   * @return a string representing the MissionMemory
   */
  @Allocate({Area.CURRENT})
  @SCJAllowed()
  public String toString() { return null; }
}
