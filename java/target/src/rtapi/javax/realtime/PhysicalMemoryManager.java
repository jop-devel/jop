package javax.realtime;
import javax.safetycritical.annotate.SCJAllowed;
import static javax.safetycritical.annotate.Level.LEVEL_1;

/**
 * This class is here just for the SPM experiments.
 * Needed in SCJ?
 * Shall go away and substituted by a SCJ concept for SPM.
 * @author martin
 *
 */
@SCJAllowed(LEVEL_1)
public final class PhysicalMemoryManager {
	
	public static final Object ON_CHIP_PRIVATE = new Object();

  /*
   *
   */

  public static final PhysicalMemoryName ALIGNED = null;
  
  /*
   *
   */  

  public static final PhysicalMemoryName BYTESWAP = null;

  /*
   *
   */
  @SCJAllowed(LEVEL_1)
  public static final PhysicalMemoryName DEVICE = null;

  /*
   *
   */
  @SCJAllowed(LEVEL_1)
  public static final PhysicalMemoryName DMA = null;

  /*
   *
   */
  @SCJAllowed(LEVEL_1)
  public static final PhysicalMemoryName IO_PAGE = null;

  /*
   *
   */
  @SCJAllowed(LEVEL_1)
  public static final PhysicalMemoryName SHARED = null;

}
