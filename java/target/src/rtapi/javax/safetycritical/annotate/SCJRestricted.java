package javax.safetycritical.annotate;

import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import static java.lang.annotation.RetentionPolicy.CLASS;

import static java.lang.annotation.ElementType.TYPE;
import static java.lang.annotation.ElementType.CONSTRUCTOR;
import static java.lang.annotation.ElementType.METHOD;

import static javax.safetycritical.annotate.Phase.ALL;

/**
 */
@Retention(CLASS)
@Target({TYPE, METHOD, CONSTRUCTOR})
public @interface SCJRestricted
{
  /** The phase of the mission in which a method may run. */
  public Phase   phase()          default ALL;
  /** Marks whether or not a method may allocate memory. */
  public boolean mayAllocate()    default true;
  /** Marks whether or not a method may execute a blocking operation. */
  public boolean maySelfSuspend() default true;
}
