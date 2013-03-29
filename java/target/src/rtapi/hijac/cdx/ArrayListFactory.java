/**
 * @author Kun Wei, Frank Zeyda
 */
package hijac.cdx;

import hijac.cdx.Error;
import hijac.cdx.javacp.utils.ArrayList;

/**
 * This class is used to pre-allocate ArrayList objects in mission memory.
 */
public class ArrayListFactory {
  private final ArrayList[] store;

  private int index;

  public ArrayListFactory(int size, int capacity) {
    store = new ArrayList[size];
    for (int i = 0; i < store.length; i++) {
      /* TODO: Why "capacity + 1" here? */
      store[i] = new ArrayList(capacity + 1);
    }
    index = 0;
  }

  /* Return a new pre-allocated instance of the ArrayList class. */

  public ArrayList getNewList() {
    if (index < store.length) {
      return store[index++];
    }
    else {
      Error.abort("Exceeding storage capacity in ArrayListFactory.");
      return null; // Never reached.
    }
  }

  /* Clear the content of each list before clearing the store. */

  public void clear() {
    for (int i = 0; i < index; i++) {
      store[i].clear();
    }
    index = 0;
  }
}
