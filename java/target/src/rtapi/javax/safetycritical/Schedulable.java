package javax.safetycritical;

import static javax.safetycritical.annotate.Level.LEVEL_2;

import javax.safetycritical.annotate.BlockFree;
import javax.safetycritical.annotate.SCJAllowed;

@SCJAllowed

/***
 * AJW want to remove this and move the method into the System class with an
 * SO with a parameter
 **/
public interface Schedulable extends Runnable {

  /**
   * Does not allocate memory. Does not allow this to escape local variables.
   * Returns an object that resides in the corresponding thread's
   * MissionMemory scope.
   */
  @BlockFree
  @SCJAllowed(LEVEL_2)
  public StorageParameters getThreadConfigurationParameters();
}

