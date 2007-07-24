/**
 * 
 */
package wcet.framework.exceptions;

/**
 * @author Elena
 * @version 0.1
 */
public class InitException extends Exception {

	public InitException(Exception e) {
		super(e);
	}

	public InitException(String string) {
		super(string);
	}

}
