/**
 * @author Kun Wei, Frank Zeyda
 */
package hijac.cdx;

import hijac.cdx.Error;
import hijac.cdx.Motion;

/**
 * This class is used to pre-allocate Motion objects in mission memory.
 */
public class MotionFactory {
  private final Motion[] store;

  private int index;

  public MotionFactory(int size) {
    store = new Motion[size];
    for (int i = 0; i < store.length; i++) {
      store[i] = new Motion();
    }
    index = 0;
  }

  /* Return a new pre-allocated instance of the Motionclass. */

  public Motion getNewMotion() {
    if (index < store.length) {
      return store[index++];
    }
    else {
      Error.abort("Exceeding storage capacity in MotionFactory.");
      return null; // Never reached.
    }
  }

  public void clear() {
    index = 0;
  }
}
