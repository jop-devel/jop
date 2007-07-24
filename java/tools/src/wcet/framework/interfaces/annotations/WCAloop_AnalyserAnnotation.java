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
public @interface WCAloop_AnalyserAnnotation{
    	final String type="WCA_loop";
	int[] value();
}
/* Usage:
 * @AnalyserAnnotation(name ="WCA_loop", value = 10)
 * public void booMethod(){
 * 	int i = 0;
 * 	while(i<10){
 * 		//do something
 * 		i++;
 * 	}
 * }
 */
