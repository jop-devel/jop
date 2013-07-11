/**
 * @author Frank Zeyda
 */
package scjlibs.examples.hijac.cdx;

/**
 * This class is used to generate program errors.
 */
public class Error {
  public static void abort(String msg) {
    System.out.println(msg);
    System.out.println("Aborting program!");
    System.exit(-1);
  }
}
