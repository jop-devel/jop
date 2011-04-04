package javax.realtime;

import javax.safetycritical.annotate.Allocate;
import javax.safetycritical.annotate.BlockFree;
import javax.safetycritical.annotate.SCJAllowed;
import javax.safetycritical.annotate.SCJProtected;

import static javax.safetycritical.annotate.Level.LEVEL_0;


/**
 * AsyncLongEventHandler
 *
 */
@SCJAllowed(LEVEL_0)
public class AsyncLongEventHandler extends AbstractAsyncEventHandler
{
 /**
   * Infrastructure code.
   * Must not be called.
   */
  @Override
  @SCJProtected
  public final void run() {}

  /**
   * @param data
   */
  @SCJAllowed
  public void handleAsyncEvent(long data) {}
}
