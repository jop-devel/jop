/**
 * @author Frank Zeyda
 */
package scjlibs.examples.hijac.cdx;

import javax.realtime.ImmortalMemory;

/* Utility class providing methods related to memory allocations. */

public class MemUtils {
  public static Object newI(Class type) {
    try {
      return ImmortalMemory.instance().newInstance(type);
    }
    catch (Exception e) {
      System.out.println(e.toString());
      System.exit(-1);
    }
    return null; // Never reached.
  }
}
