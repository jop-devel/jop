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
 * indicates that the argument named by the outer array must reside at
 * the same scope level as the argument named by the inner array.
 */
@Retention(CLASS)
@Target({METHOD, CONSTRUCTOR})
public @interface MemoryAreaSame
{
  public String[] inner() default {};

  public String[] outer() default {};
}
