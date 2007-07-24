/**
 * 
 */
package wcet.framework.interfaces.annotations;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.lang.annotation.ElementType;
/**
 * @author Elena Axamitova
 * @version 0.1 15.03.2007
 */
@Retention(RetentionPolicy.CLASS)
@Target(ElementType.METHOD)
public @interface AnalyserAnnotation {
    	String type();
	int[] value();
}


