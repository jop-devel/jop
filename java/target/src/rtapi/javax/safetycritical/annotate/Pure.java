package javax.safetycritical.annotate;

import java.lang.annotation.Documented;
import java.lang.annotation.Inherited;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;
import static java.lang.annotation.RetentionPolicy.CLASS;
import static java.lang.annotation.ElementType.METHOD;

@Documented
@Inherited
@Retention(CLASS)
@Target({METHOD})
public @interface Pure
{
  public Degree value() default Degree.IMMUTABLE;
  public static enum Degree { IMMUTABLE, READ, CACHED };
}
