/*---------------------------------------------------------------------*\
 *
 * aicas GmbH, Karlsruhe, 2010
 *
 * This file may be modified for and distributed with the JSR 282 reference
 * implementation under any license deemed appropriate by the specification
 * leader provided that this notice and authorship is maintained.  aicas GmbH
 * reserves the right to use this class as it is or in any derived form.
 *
 * $Source: /home/cvs/jsr302/scj/specsrc/javax/safetycritical/ManagedMemory.java,v $
 * $Revision: 1.8 $
 * $Author: jjh $
 * Contents: javax.safetycritical.ManagedMemory
 *
\*---------------------------------------------------------------------*/
package javax.safetycritical;


import javax.realtime.LTMemory;
import javax.realtime.SizeEstimator;
import javax.safetycritical.annotate.SCJAllowed;

/**
 * This is the base class for all safety critical Java memory areas.
 * The class provides a uniform method of retrieving the mission manager
 * of the memory areas' mission.
 */
@SCJAllowed
public abstract class ManagedMemory extends LTMemory
{
  ManagedMemory(long size) { super(size); }
  
  ManagedMemory(SizeEstimator estimator) { super(estimator); }
  
  /**
   * @return
   */
  @SCJAllowed
  public static ManagedMemory getCurrentManagedMemory() { return null; }
  
  /**
   * @return
   */
  @SCJAllowed
  public static long getMaxManagedMemorySize() {return 0l; }

  /**
   * @return the manager for this memory area.
   */
  MissionManager getManager() { return null; }

  /**
   * @param size
   * @param logic
   */
  @SCJAllowed
  public void enterPrivateMemory(long size, Runnable logic) {}

  /**
   * @return
   */
  @SCJAllowed
  public ManagedSchedulable getOwner() { return null; }
}
