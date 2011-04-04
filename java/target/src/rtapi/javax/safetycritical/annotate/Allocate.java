package javax.safetycritical.annotate;

import static java.lang.annotation.ElementType.CONSTRUCTOR;
import static java.lang.annotation.ElementType.METHOD;
import static java.lang.annotation.RetentionPolicy.CLASS;

import java.lang.annotation.Retention;
import java.lang.annotation.Target;

/**
 * This annotation identifies the allocation behavior of the method or
 * constructor.  The value attribute is an array of AreaName objects
 * representing certain named memory areas.  The sameAreaAs attribute
 * holds string names corresponding to the names of the method or
 * constructor's arguments.  If a particular argument is named in the
 * sameAreaAs array, this indicates that the method or constructor may
 * allocate memory in the scope where that argument resides.
 */
@Retention(CLASS)
@Target({METHOD, CONSTRUCTOR})
public @interface Allocate
{
  /**
   * SCOPED indicates that this routine allocates ScopedMemory
   * backing store from the current thread's stack.
   */
  public static enum Area { IMMORTAL, MISSION, CURRENT, THIS, SCOPED }
  
  public Area[] value() default {};

  /**
   * This array of strings contains arguments names of the method.  The
   * string <code>"this"</code> refers to the implicit
   * <code>this</code> argument passed to instance methods.  Appending a
   * <code>".area"</code> to any variable name, including this,
   * indicates that the corresponding argument is of type MemoryArea,
   * with at least one allocation is taken from the corresponding
   * MemoryArea's backing store.
   */
  public String[] sameAreaAs() default {};
}
