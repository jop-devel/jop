package javax.safetycritical.annotate;

import static java.lang.annotation.ElementType.CONSTRUCTOR;
import static java.lang.annotation.ElementType.METHOD;
import static java.lang.annotation.RetentionPolicy.CLASS;

import java.lang.annotation.Retention;
import java.lang.annotation.Target;

/**
 * This annotation distinguishes methods that do not block (i.e., no
 * wait, no blocking I/O, no self suspension, no calls to other methods
 * that are not also @BlockFree.
 */
@Retention(CLASS)
@Target({METHOD, CONSTRUCTOR})
public @interface BlockFree
{
}
