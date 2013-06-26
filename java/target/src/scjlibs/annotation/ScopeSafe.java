package annotation;

import static java.lang.annotation.ElementType.METHOD;
import static java.lang.annotation.RetentionPolicy.CLASS;

import java.lang.annotation.Retention;
import java.lang.annotation.Target;

/**
 * This annotation means that the method does not perform memory allocations.
 * It does not use the <code>new</code> keyword or any other method that it 
 * calls does not perform memory allocations. 
 *  
 * @author jrri
 * 
 */
@Retention(CLASS)
@Target({METHOD})
public @interface ScopeSafe {

}
