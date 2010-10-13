package javax.safetycritical.annotate;

import static java.lang.annotation.ElementType.TYPE;
import static java.lang.annotation.RetentionPolicy.CLASS;

import java.lang.annotation.Documented;
import java.lang.annotation.Inherited;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

/** Immutable Annotation */
@Documented
@Inherited
@Retention(CLASS)
@Target({TYPE})
public @interface Immutable {}

