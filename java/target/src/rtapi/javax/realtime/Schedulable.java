package javax.realtime;

import javax.safetycritical.annotate.BlockFree;
import javax.safetycritical.annotate.SCJAllowed;

import static javax.safetycritical.annotate.Level.LEVEL_2;

@SCJAllowed
public interface Schedulable extends Runnable {

  /**
   * Does not allocate memory. Does not allow this to escape local variables.
   * Returns an object that resides in the corresponding thread's
   * MissionMemory scope.
   */
  public MemoryParameters getMemoryParameters();
   

  /**
   * Does not allocate memory. Does not allow this to escape local variables.
   * Returns an object that resides in the corresponding thread's
   * MissionMemory scope.
   */
  //@BlockFree
  //@SCJAllowed(LEVEL_2)
  public ReleaseParameters getReleaseParameters();

  /**
   * Does not allocate memory. Does not allow this to escape local variables.
   * Returns an object that resides in the corresponding thread's
   * MissionMemory scope.
   */
  //@BlockFree
  //@SCJAllowed(LEVEL_2)*/
  public SchedulingParameters getSchedulingParameters();
}

