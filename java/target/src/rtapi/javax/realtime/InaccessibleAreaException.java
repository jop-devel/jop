package javax.realtime;

// import java.io.Serializable;

import javax.safetycritical.annotate.Allocate;
import javax.safetycritical.annotate.BlockFree;
import javax.safetycritical.annotate.MemoryAreaEncloses;
import javax.safetycritical.annotate.SCJAllowed;

import static javax.safetycritical.annotate.Allocate.Area.CURRENT;

/**
 * TBD: do we make this SCJAllowed?  It may be that the restrictions
 * put in place for JSR 302 code will guarantee that this exception is
 * never thrown.  However, such restrictions are not yet sufficiently
 * defined to allow this determination.
 */
@SCJAllowed
public class InaccessibleAreaException
  extends RuntimeException { // implements Serializable {

  /**
   * Shall not copy "this" to any instance or
   * static field.
   * <p>
   * Allocates an application- and implementation-dependent amount of
   * memory in the current scope (to represent stack backtrace).
   */
  @Allocate({CURRENT})
  @BlockFree
  @SCJAllowed
  public InaccessibleAreaException() {
  }

  /**
   * Shall not copy "this" to any instance or
   * static field. The scope containing the msg argument must enclose the
   * scope containing "this". Otherwise, an IllegalAssignmentError will be
   * thrown.
   * <p>
   * Allocates an application- and implementation-dependent amount of
   * memory in the current scope (to represent stack backtrace).
   */
  @Allocate({CURRENT})
  @BlockFree
  @MemoryAreaEncloses(inner = {"this"}, outer = {"msg"})
  @SCJAllowed
  public InaccessibleAreaException(String description) {
    super(description);
  }
}
