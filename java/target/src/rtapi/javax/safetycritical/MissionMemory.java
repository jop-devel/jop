
package javax.safetycritical;

import static javax.safetycritical.annotate.Level.LEVEL_1;

import javax.safetycritical.annotate.Allocate;
import javax.safetycritical.annotate.BlockFree;
import javax.safetycritical.annotate.MemoryAreaEncloses;
import javax.safetycritical.annotate.SCJAllowed;
import javax.safetycritical.annotate.Allocate.Area;

/**
 * MissionMemory is basically an empty class and might go.
 *
 */
@SCJAllowed
class MissionMemory extends ManagedMemory
{
  /**
   * Package private constructor
   * @param size is the amount of memory that this area can hold.
   */
//  MissionMemory(int size) 
//  { 
//	super(size);
//	
//  }

}
