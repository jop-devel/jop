package javax.safetycritical.annotate;

import static java.lang.annotation.ElementType.CONSTRUCTOR;
import static java.lang.annotation.ElementType.FIELD;
import static java.lang.annotation.ElementType.METHOD;
import static java.lang.annotation.ElementType.TYPE;
import static java.lang.annotation.RetentionPolicy.CLASS;
import static javax.safetycritical.annotate.Level.LEVEL_0;

import java.lang.annotation.Retention;
import java.lang.annotation.Target;

/**
 * This annotation distinguishes methods, classes, and fields that may
 * be accessed from within safety-critical Java programs.  In some
 * implementations of the safety-critical Java specification,
 * elements which are not declared with the @SCJAllowed annotation
 * (and are therefore not allowed in safety-critical application
 * software) are present within the declared class hierarchy.  These
 * are necessary for full compatibility with standard edition Java, the
 * Real-Time Specification for Java, and/or for use by the
 * implementation of infrastructure software.
 * <p>
 * The value field equals LEVEL_0 for elements that may be used within
 * safety-critical Java applications targeting levels 0, 1, or 2.
 * <p>
 * The value field equals LEVEL_1 for elements that may be used within
 * safety-critical Java applications targeting levels 1 or 2.
 * <p>
 * The value field equals LEVEL_2 for elements that may be used within
 * safety-critical Java applications targeting level 2.
 * <p>
 * Absence of this annotation on a given Class, Field, Method, or
 * Constructor declaration indicates that the corresponding element
 * may not be accessed from within a complaint safety-critical Java
 * application. 
 */
@Retention(CLASS)
@Target({TYPE, FIELD, METHOD, CONSTRUCTOR})
public @interface SCJAllowed
{
  public Level value() default LEVEL_0;
  public boolean members() default false;
}
