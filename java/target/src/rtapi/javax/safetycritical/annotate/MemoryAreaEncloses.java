package javax.safetycritical.annotate;


import static java.lang.annotation.ElementType.CONSTRUCTOR;
import static java.lang.annotation.ElementType.METHOD;
import static java.lang.annotation.RetentionPolicy.CLASS;

import java.lang.annotation.Retention;
import java.lang.annotation.Target;

/**
 * This annotation identifies required nesting relationships between
 * incoming arguments, including "this".  Each entry within the
 * inner[] array is compared with the corresponding entry within the
 * outer[] array.  For each pair-wise comparison, this annotation
 * indicates that the argument named by the outer array must reside in a
 * scope that encloses the scope of the argument named by the inner
 * array.  To say that one scope encloses another is to indicate that
 * the first scope is the same as, or nested external to the second
 * scope. 
 * <p>
 * TBD: Consider Class.getEnumConstants().  Need a way to say that the
 * classloader for the Enumeration must enclose the scope that holds
 * the result returned from the method.  Also consider
 * Class.newInstance().
 * <p>
 * TBD: Also, consider AbsoluteTime(AbsoluteTime time) constructor.
 * We don't require that time enclose "this".  However, we need to be
 * able to say that time.getClock() enclose "this".
 */
@Retention(CLASS)
@Target({METHOD, CONSTRUCTOR})
public @interface MemoryAreaEncloses
{
  public String[] inner() default {};

  public String[] outer() default {};
}
